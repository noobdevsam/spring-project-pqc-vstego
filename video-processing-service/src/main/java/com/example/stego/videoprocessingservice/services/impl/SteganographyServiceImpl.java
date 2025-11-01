package com.example.stego.videoprocessingservice.services.impl;

import com.example.stego.videoprocessingservice.services.SteganographyService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SteganographyServiceImpl implements SteganographyService {

    private static final byte[] PAYLOAD_TERMINATOR = "PQCSTEGO_END".getBytes();
    private final RestClient fileServiceRestClient;
    private final String FILE_SERVICE_UPLOAD_URI = "/api/v1/files/upload";


    @Override
    public void embedPayload(
            InputStream carrierVideoStream,
            byte[] payloadData,
            OutputStream stegoVideoOutputStream
    ) throws IOException, InterruptedException {

        // ffmpeg command to extract raw video frames (rgba)
        var extractBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", "pipe:0",          // Input from stdin
                "-f", "rawvideo",        // Output format raw video
                "-pix_fmt", "rgba",      // Pixel format
                "pipe:1"                 // Output to stdout
        );

        // ffmpeg command to re-assemble raw frames into an MP4 video
        var assembleBuilder = new ProcessBuilder(
                "ffmpeg",
                "-f", "rawvideo",
                "-pix_fmt", "rgba",
                "-s", "infer_size",      // Will be replaced with actual size
                "-r", "30",              // Assume 30fps, should be inferred
                "-i", "pipe:0",          // Input from stdin
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                "-f", "mp4",
                "pipe:1"                 // Output to stdout
        );

        // First, get video dimensions using ffprobe
        Map<String, Object> videoInfo = getVideoInfo(carrierVideoStream);
        var width = ((Double) videoInfo.get("width")).intValue();
        var height = ((Double) videoInfo.get("height")).intValue();
        var frameRate = (String) videoInfo.get("avg_frame_rate");

        // Update assembler with correct size and framerate
        assembleBuilder.command().set(5, width + "x" + height);
        assembleBuilder.command().set(7, frameRate);

        var extractor = extractBuilder.start();
        var assembler = assembleBuilder.start();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Thread to pipe carrier video to extractor's stdin
            executor.submit(() -> {
                try (OutputStream extractorStdin = extractor.getOutputStream()) {
                    carrierVideoStream.transferTo(extractorStdin);
                } catch (IOException e) {
                    log.error("Error piping carrier video to ffmpeg", e);
                }
            });

            // Thread to handle embedding and piping between processes
            executor.submit(() -> {
                try (InputStream extractorStdout = extractor.getInputStream();
                     OutputStream assemblerStdin = assembler.getOutputStream()) {
                    embedStream(extractorStdout, payloadData, assemblerStdin, width, height);
                } catch (IOException e) {
                    log.error("Error during embedding stream processing", e);
                }
            });

            // Thread to pipe assembler's output to the final stego video stream
            executor.submit(() -> {
                try (InputStream assemblerStdout = assembler.getInputStream()) {
                    assemblerStdout.transferTo(stegoVideoOutputStream);
                } catch (IOException e) {
                    log.error("Error piping final video to output stream", e);
                } finally {
                    try {
                        stegoVideoOutputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            });

            // Wait for processes to finish
            int extractExitCode = extractor.waitFor();
            int assembleExitCode = assembler.waitFor();
            if (extractExitCode != 0 || assembleExitCode != 0) {
                throw new IOException("ffmpeg process exited with non-zero code. Extraction: " + extractExitCode + ", Assembly: " + assembleExitCode);
            }
        }

    }

    private void embedStream(
            InputStream rawFrames,
            byte[] payloadData,
            OutputStream output,
            int width,
            int height
    ) throws IOException {

        var payloadWithTerminator = new ByteArrayOutputStream();
        payloadWithTerminator.write(payloadData);
        payloadWithTerminator.write(PAYLOAD_TERMINATOR);
        var fullPayload = payloadWithTerminator.toByteArray();

        int payloadBitIndex = 0;
        int frameSize = width * height * 4; // RGBA
        var frameBuffer = new byte[frameSize];

        // Read each frame and embed payload bits
        while (rawFrames.read(frameBuffer) != -1) {

            // Check if there's still payload to embed
            if (payloadBitIndex < fullPayload.length * 8) {

                // Embed payload bits into the frame's pixel data
                for (int i = 0; i < frameBuffer.length && payloadBitIndex < fullPayload.length * 8; i++) {
                    byte payloadByte = fullPayload[payloadBitIndex / 8];
                    int bit = (payloadByte >> (7 - (payloadBitIndex % 8))) & 1;
                    frameBuffer[i] = (byte) ((frameBuffer[i] & 0xFE) | bit); // Set LSB
                    payloadBitIndex++;
                }

            }

            // Write modified frame to output
            output.write(frameBuffer);
        }

    }

    @Override
    public byte[] extractPayload(
            InputStream stegoVideoInputStream
    ) throws IOException, InterruptedException {

        var extractBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", "pipe:0",
                "-f", "rawvideo",
                "-pix_fmt", "rgba",
                "pipe:1"
        );
        var extractor = extractBuilder.start();

        try (var extractorStdin = extractor.getOutputStream();
             var extractorStdout = extractor.getInputStream();
             var extractedPayload = new ByteArrayOutputStream()) {

            // Pipe stego video to extractor in a separate thread
            var pipeToStdin = new Thread(() -> {
                try {
                    stegoVideoInputStream.transferTo(extractorStdin);
                } catch (IOException e) {
                    log.error("Error piping stego video to ffmpeg", e);
                } finally {
                    try {
                        extractorStdin.close();
                    } catch (IOException ignored) {
                    }
                }
            });

            // Start the piping thread
            pipeToStdin.start();

            // Extract LSBs from the raw frame stream
            byte currentByte = 0;
            int bitCount = 0;
            var buffer = new byte[1024 * 4]; // Read 1k pixels at a time
            int bytesRead;

            // Read until we find the terminator
            while ((bytesRead = extractorStdout.read(buffer)) != -1) {

                // Process each byte to extract LSBs
                for (int i = 0; i < bytesRead; i++) {
                    var lsb = (byte) (buffer[i] & 1);  // Get LSB
                    currentByte |= (lsb << (7 - bitCount));
                    bitCount++;

                    // If we have a full byte, write it to the payload
                    if (bitCount == 8) {
                        extractedPayload.write(currentByte);
                        // Check for terminator
                        var currentTail = extractedPayload.toByteArray();

                        // Check if the terminator is at the end
                        if (currentTail.length > PAYLOAD_TERMINATOR.length) {
                            boolean found = true;

                            // Compare last bytes with terminator
                            for (int j = 0; j < PAYLOAD_TERMINATOR.length; j++) {
                                if (currentTail[currentTail.length - PAYLOAD_TERMINATOR.length + j] != PAYLOAD_TERMINATOR[j]) {
                                    found = false;
                                    break;
                                }
                            }

                            if (found) {
                                // Terminator found, return payload without it
                                var finalPayload = new byte[currentTail.length - PAYLOAD_TERMINATOR.length];
                                System.arraycopy(currentTail, 0, finalPayload, 0, finalPayload.length);
                                return finalPayload;
                            }
                        }

                        currentByte = 0;
                        bitCount = 0;
                    } // End of full byte check

                } // End of buffer processing

            } // End of stream reading
            throw new IOException("Payload terminator not found in video stream.");
        } finally {
            extractor.destroy(); // Ensure process is terminated
        }

    }

    @Override
    public Map<String, Object> getVideoInfo(InputStream videoStream) throws IOException, InterruptedException {

        var ffprobeBuilder = new ProcessBuilder(
                "ffprobe",
                "-v", "quiet",
                "-print_format", "json",
                "-show_streams",
                "-i", "pipe:0"
        );
        var ffprobe = ffprobeBuilder.start();

        try (var stdin = ffprobe.getOutputStream()) {
            videoStream.transferTo(stdin);
        }

        try (InputStream stdout = ffprobe.getInputStream()) {
            var jsonOutput = new String(stdout.readAllBytes());
            var gson = new Gson();
            Map<String, Object> result = gson.fromJson(jsonOutput, Map.class);
            return (Map<String, Object>) ((List) result.get("streams")).get(0);
        } finally {
            ffprobe.waitFor();
            ffprobe.destroy();
        }
    }

    @Override
    public String uploadFile(InputStream fileStream, String fileName, String contentType, String ownerId) {
        return "";
    }

    // Custom resource class to handle streaming from an InputStream
    private static class MyInputStreamResource extends InputStreamResource {

        public MyInputStreamResource(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public String getFilename() {
            return "stream"; // Required for multipart requests
        }

        @Override
        public long contentLength() {
            return -1; // Let the stream handle its length
        }
    }

}
