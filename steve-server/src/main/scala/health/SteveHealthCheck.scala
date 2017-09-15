package health

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result

class SteveHealthCheck extends HealthCheck {
  override def check(): Result = Result.healthy
}
