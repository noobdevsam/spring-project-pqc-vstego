package com.example.stego.videoprocessingservice.services.impl;

import com.example.stego.videoprocessingservice.services.SteganographyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    }

    @Override
    public byte[] extractPayload(InputStream stegoVideoInputStream) throws IOException, InterruptedException {
        return new byte[0];
    }

    @Override
    public Map<String, Object> getVideoInfo(InputStream videoStream) throws IOException, InterruptedException {
        return Map.of();
    }

    @Override
    public String uploadFile(InputStream fileStream, String fileName, String contentType, String ownerId) {
        return "";
    }

}
