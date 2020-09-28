package cn.whu.wy.schedule.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

/**
 * @Author WangYong
 * @Date 2020/03/24
 * @Time 21:16
 */
@Component
@Slf4j
public class ShellExecutor {
    @Autowired
    private FileWriteService fileWriteService;

    private static ExecutorService pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 3L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    public ExecuteResult executeCommand(String command, long timeout) {
        Process process = null;
        InputStream pIn = null;
        InputStream pErr = null;
        StreamGobbler outputGobbler = null;
        StreamGobbler errorGobbler = null;
        Future<Integer> executeFuture = null;
        try {
            process = Runtime.getRuntime().exec(command);
            final Process p = process;

            // close process's output stream.
            p.getOutputStream().close();

            pIn = process.getInputStream(); // 获取子进程的输入流（shell命令的返回值）。

            outputGobbler = new StreamGobbler(pIn, "OUTPUT");
            outputGobbler.start();

            pErr = process.getErrorStream();
            errorGobbler = new StreamGobbler(pErr, "ERROR");
            errorGobbler.start();

            // create a Callable for the command's Process which can be called by an Executor
            Callable<Integer> call = new Callable<Integer>() {
                public Integer call() throws Exception {
                    p.waitFor();
                    return p.exitValue();
                }
            };

            // submit the command's call and get the result from a
            executeFuture = pool.submit(call);
            int exitCode = executeFuture.get(timeout, TimeUnit.MILLISECONDS);
//            fileWriteService.write("./shell-stderr.log", "command=" + command);
//            fileWriteService.write("./shell-stderr.log", errorGobbler.getContent());
            return new ExecuteResult(exitCode, errorGobbler.getContent() + outputGobbler.getContent());
        } catch (IOException ex) {
            String errorMessage = "The command [" + command + "] execute failed.";
            log.error(errorMessage, ex);
            return new ExecuteResult(-1, errorMessage);
        } catch (TimeoutException ex) {
            String errorMessage = "The command [" + command + "] timed out.";
            log.error(errorMessage, ex);
            return new ExecuteResult(-1, errorMessage);
        } catch (ExecutionException ex) {
            String errorMessage = "The command [" + command + "] did not complete due to an execution error.";
            log.error(errorMessage, ex);
            return new ExecuteResult(-1, errorMessage);
        } catch (InterruptedException ex) {
            String errorMessage = "The command [" + command + "] did not complete due to an interrupted error.";
            log.error(errorMessage, ex);
            return new ExecuteResult(-1, errorMessage);
        } finally {
            if (executeFuture != null) {
                try {
                    executeFuture.cancel(true);
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
            if (pIn != null) {
                this.closeQuietly(pIn);
                if (outputGobbler != null && !outputGobbler.isInterrupted()) {
                    outputGobbler.interrupt();
                }
            }
            if (pErr != null) {
                this.closeQuietly(pErr);
                if (errorGobbler != null && !errorGobbler.isInterrupted()) {
                    errorGobbler.interrupt();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    private void closeQuietly(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
            log.error("exception", e);
        }
    }
}
