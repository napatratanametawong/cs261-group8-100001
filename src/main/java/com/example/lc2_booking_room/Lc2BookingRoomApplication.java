package com.example.lc2_booking_room;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.lc2_booking_room.repository.RoomRepository;

@SpringBootApplication
public class Lc2BookingRoomApplication {
  public static void main(String[] args) {
    SpringApplication.run(Lc2BookingRoomApplication.class, args);
  }
}
