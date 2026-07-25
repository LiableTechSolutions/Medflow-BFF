package com.medflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@Modulithic
@SpringBootApplication
public class MedflowApplication {
  public static void main(String[] args) { SpringApplication.run(MedflowApplication.class, args); }
}
