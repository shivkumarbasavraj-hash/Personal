package com.rental.service;

import com.rental.model.Car;
import com.rental.model.CarType;
import com.rental.model.Reservation;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.collectors;

public class CarRentalSystem{
  private final Map<CarType, List<Car>> inventory;
  private final Map<String, Reservation> reservations;
  private final AtomicLong reservationIdGenerator;

public CarRentalSystem(){
  this.inventory=new ConcuurentHashMap<>();
  this.reservations=new ConcurrentHashMap<>();
  this.reservationIdGenerator=new AtomicLong(1);

  for (CarType type: CarType.Values()){
    inventory.put(type, new ArrayList<>());
  }
}

public void addCar(Car car)
  {
    Objects.requireNonNull(Car, "Car cannot be null");
    List<Car> cars=inventory.get(car.getType());
    synchronized(cars){
      if(cars.stream().anyMatch(c->c.getId().equals(car.getId()))) {
        throw new IllegalArgumentException("Car with ID" + car.getId() + "already exists");
      }
      cars.add(car);
    }
  }
        
public void addCars(List<Car> cars) {
  cars.forEach(this::addCar);
}

  public int getInventoryCount(CarType type){
    return inventory.get(type).size();
  }

  public int getTotalInventoryCount(){
    return inventory.values().stream().mapToInt(List::Size).sum();
  }

  public Reservation makeReservation(CarType cartype, LocalDateTime startDateTime, int numberOfDays, String customerName){
    Objects.requireNonNull(carType, "Car type cannot be null");
     Objects.requireNonNull(startDateTime, "Start date/time type cannot be null");
     Objects.requireNonNull(customerName, "customerName cannot be null");

    if (numberOfDays<=0){
      throw new IllegalArgumentException("Number of days must be positive");
    }

List<Car> availableCars =getAvailableCars(carType, startDateTime, numberOfDays);

    if(availableCars.isEmpty()){
      throw new NoSuchElementException(
        String.format("No %s available from %s for %d days",carType.getDisplayName(), startDateTime,numberOfDays));
    }

    Car selectedCar=availableCars.get(0);
    String reservatioId="RES-" + reservationIdGenerator.getAndIncrement();

    Reservation reservation = new Reservation(reservationId, selectedCar, customerName, startDateTime, numberOfDays);
    reservations.put(reservationId, reservation);

    return reservation;
  }

  public List<Car> getAvailableCars(CarType carType, LocalDateTime startDateTime, int numberOfDays){
     Objects.requireNonNull(carType, "Car type cannot be null");
     Objects.requireNonNull(startDateTime, "Start date/time type cannot be null");

    if (numberOfDays<=0){
      throw new IllegalArgumentException("Number of days must be positive");
    }

    LocalDateTime endDateTime=startDateTime.plusDays(numberOfDays);
    List<Car> carsOfType = inventory.get(carType);

    synchronized(carsOfType){
      return carsOfType.stream().filter(car->isCarAvailable(car,startDateTime,endDateTime)).collect(collectors.toList());
    }
  }

  private boolean isCarAvalable(Car car, LocalDateTime startDateTime, LocalDateTime endDateTime){
    return reservations.values().stream().filter(r->getCar().equals(car)).noneMatch(r->r.overlaps(startDateTime,endDateTime));
  }

  public boolean cancelReservation(String reservationId){
     Objects.requireNonNull(reservationId, "reservationId cannot be null");
    return reservations.remove(reservationId)!=null;
  }

  public Optional<Reservation> getReservation(String reservationId){
    return Optional.ofNullable(reservations.get(reservationId);
  }
  
public List<Reservation> getAllReservations(){
  return new ArrayList<>(reservations.values());

  public List<Reservation> getReservationsByCustomer(String customerName){
    return reservations.values().stream().filter(r->r.getCustomerName().equals(customerName)).collect(collectors.toList());
}

  public List<Reservation> getReservationsForCar(String CarId){
    return reservations.values().stream().filter(r->r.getCar().getId().equals(carId)).collect(collectors.toList());
}

  public List<Car> getAllCars(){
    return inventory.values().stream().flatMap(List::stream).collect(collectors.toList());
}

  public List<Car> getCarsByType(CarType type){
    return new ArrayList<>(inventory.get(type));
  }
}



    
  
