package com.chatforyou.io.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * FunctionalInterface 를 이용한 Thread Job 구현
 * ThreadUtils.runTask(()-> {job}, {retury}, {sleep}, {jobName});
 */
@Slf4j
public class ThreadUtils {
    private ThreadUtils(){
    }

    @FunctionalInterface
    public interface Task {
        void execute();
    }

    /**
     *  함수형 인터페이스를 통해 Tread 로 특정 task 를 실행
     *  실패 시 최대 retry 만큼 반복, 반복시 sleep 만큼 대기
     * @param task 실행할 job
     * @param retry 반복 횟수
     * @param sleep 반복시 대기 시간
     * @param jobName job 이름
     */
    public static void runTask(Task task, int retry, long sleep, String jobName) {
        Thread thread = new Thread(() -> {
            int attempts = 0;
            boolean isSuccess = false;
            while (attempts < retry && !isSuccess) {
                try {
                    task.execute();
                    isSuccess = true;
                    log.info("=== {} Job Success ===", jobName);
                } catch (Exception e) {
                    attempts++;
                    if (attempts < retry) {
                        log.info("=== try {} Job :: {}", jobName, attempts);
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException ie) {
                            // 스레드 대기 중 인터럽트 예외 처리
                            Thread.currentThread().interrupt();
                        }

                    } else {
                        log.warn("=== {} Job Failed :: {}", jobName, attempts);
                    }
                }
            }
        });
        thread.start();
    }
}
