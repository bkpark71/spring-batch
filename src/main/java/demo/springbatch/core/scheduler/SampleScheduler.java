package demo.springbatch.core.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class SampleScheduler {
  @Autowired
  private Job helloWorldJob;
  @Autowired
  private JobLauncher jobLauncher; // scheduling을 이용하여 job을 실행하기 위해 필요함

  @Scheduled(cron="0 */1 * * * *") // 크론탭 초분시간일주 --> 두번째 옵션으로 1분마다 실행하도록 설정
  public void helloWorldJobRun() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
    JobParameters jobParameters = new JobParameters(
        Collections.singletonMap("requestTime", new JobParameter(System.currentTimeMillis())) //반복실행을 위해 값을 변경시켜줘야 함.
    );
    jobLauncher.run(helloWorldJob, jobParameters); // helloworldjob을 autowired로 주입받아서 실행, 파라미터의 값이 같으면
                                                  // spring batch 에서 반복실행하지 않으므로 값을 변경시켜줘야 함.
  }

}
