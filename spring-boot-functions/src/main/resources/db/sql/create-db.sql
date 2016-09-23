CREATE TABLE IF NOT EXISTS app_method_execution_time (
  oid         INTEGER AUTO_INCREMENT PRIMARY KEY,
  thread_name VARCHAR(100),
  class_name  VARCHAR(100),
  method_name VARCHAR(50),
  start_time  TIMESTAMP,
  end_time TIMESTAMP,
  duration	  INTEGER,
  exec_status  VARCHAR(10)
);