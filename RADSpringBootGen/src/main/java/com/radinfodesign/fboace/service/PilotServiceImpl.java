package com.radinfodesign.fboace.service;

import static java.lang.System.out;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.radinfodesign.fboace.dao.AircraftTypeRepository;
import com.radinfodesign.fboace.dao.FlightCrewMemberRepository;
import com.radinfodesign.fboace.dao.FlightRepository;
import com.radinfodesign.fboace.dao.PilotCertificationRepository;
import com.radinfodesign.fboace.dao.PilotRareThingRepository;
import com.radinfodesign.fboace.dao.PilotRepository;
import com.radinfodesign.fboace.model.AircraftType;
import com.radinfodesign.fboace.model.Flight;
import com.radinfodesign.fboace.model.FlightCrewMember;
import com.radinfodesign.fboace.model.Pilot;
import com.radinfodesign.fboace.model.PilotCertification;
import com.radinfodesign.fboace.model.PilotRareThing;

@Service
public class PilotServiceImpl implements PilotService{
  
  @Autowired
  PilotRepository pilotRepository; 
  @Autowired
  PilotCertificationRepository pilotCertificationRepository;
  @Autowired
  AircraftTypeRepository aircraftTypeRepository;
  @Autowired
  FlightCrewMemberRepository flightCrewMemberRepository;
  @Autowired
  FlightRepository flightRepository;
  @Autowired
  PilotRareThingRepository pilotRareThingRepository; 
  
  @Override
  public List<Pilot> getAll() {
    return pilotRepository.findAllInDefaultOrder();
  }
  
  @Override
  public Pilot getEntity (Integer pilotId) {
    return pilotRepository.findOne(pilotId);
  }
 
  @Override
  @Transactional
  public Pilot putEntity 
       ( Integer pilotId
       , String lastName
       , String firstName
       , String middleInitial
       , String nationalIdNumber
       , String birthdate
       , String notes
       , String[] pilotCertificationCertificationNumbers
       , String[] pilotCertificationValidFromDates
       , String[] pilotCertificationExpirationDates
       , String[] pilotCertificationNotess
       , Integer[] pilotCertificationAircraftTypeIds
       , String[] flightCrewMemberNotess
       , Integer[] flightCrewMemberFlightIds
       , String[] pilotRareThingShortNames
       , String[] pilotRareThingLongNames
       , String[] pilotRareThingNotess
       ) throws Exception
  {
    out.println("PilotServiceClass.putEntity()");
    Pilot pilot;
    if (pilotId==null || pilotId==0) pilot = new Pilot();
    else pilot = pilotRepository.findOne(pilotId);
    
    LocalDate birthdate_ = null;
    try {
       birthdate_ = LocalDate.parse(birthdate, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException e) {
      out.println("No good value for birthdate: " + e.getMessage());
    }
    pilot.setBirthdate(birthdate_);
    
    pilot.setLastName(lastName);
    pilot.setFirstName(firstName);
    pilot.setMiddleInitial(middleInitial);
    pilot.setNationalIdNumber(nationalIdNumber);
    pilot.setNotes(notes);
    
    pilot = pilotRepository.save(pilot);
    pilotId = pilot.getPilotId();
    System.out.println("PilotServiceImpl.putEntity: id = " + pilot.getPilotId());

    AircraftType aircraftType = null;
    Flight flight = null;
    
    PilotCertification pilotCertification = null;
    if (pilotCertificationAircraftTypeIds != null) {
      final int NEW_RECORD = pilotCertificationAircraftTypeIds.length-1; // ASSUME LAST RECORD REPRESENTS NEW/BLANK TO BE INSERTED.
      for (int i=0; i<pilotCertificationAircraftTypeIds.length; i++) {
        aircraftType = aircraftTypeRepository.findOne(pilotCertificationAircraftTypeIds[i]);
        if (aircraftType == null | pilot == null) continue; // pilot should NEVER be null at this point
        if (i==NEW_RECORD) {
          pilotCertification = new PilotCertification(pilot, aircraftType);
          out.println("NEW pilotCertification = " + pilotCertification);
        }
        else {
          pilotCertification = pilotCertificationRepository.findOneByPilotAndAircraftType(pilot, aircraftType);
        }
        LocalDate validFromDate_ = null;
        try {
           validFromDate_ = LocalDate.parse(pilotCertificationValidFromDates[i], DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
          out.println("No good value for validFromDate: " + e.getMessage());
        }
        pilotCertification.setValidFromDate(validFromDate_);
        LocalDate expirationDate_ = null;
        try {
           expirationDate_ = LocalDate.parse(pilotCertificationExpirationDates[i], DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
          out.println("No good value for expirationDate: " + e.getMessage());
        }
        pilotCertification.setExpirationDate(expirationDate_);
        
        if (pilotCertificationCertificationNumbers.length > 0)
          pilotCertification.setCertificationNumber(pilotCertificationCertificationNumbers[i]);
        if (pilotCertificationNotess.length > 0)
          pilotCertification.setNotes(pilotCertificationNotess[i]);
        
        if (i==NEW_RECORD) {
          int insertResult = pilotCertificationRepository.insertPilotCertification(pilotCertification);
        }
        else {
          pilotCertification = pilotCertificationRepository.save(pilotCertification);
        }
      }
    }
    
    FlightCrewMember flightCrewMember = null;
    if (flightCrewMemberFlightIds != null) {
      final int NEW_RECORD = flightCrewMemberFlightIds.length-1; // ASSUME LAST RECORD REPRESENTS NEW/BLANK TO BE INSERTED.
      for (int i=0; i<flightCrewMemberFlightIds.length; i++) {
        flight = flightRepository.findOne(flightCrewMemberFlightIds[i]);
        if (flight == null | pilot == null) continue; // pilot should NEVER be null at this point
        if (i==NEW_RECORD) {
          flightCrewMember = new FlightCrewMember(flight, pilot);
          out.println("NEW flightCrewMember = " + flightCrewMember);
        }
        else {
          flightCrewMember = flightCrewMemberRepository.findOneByFlightAndPilot(flight, pilot);
        }
        
        if (flightCrewMemberNotess.length > 0)
          flightCrewMember.setNotes(flightCrewMemberNotess[i]);
        
        if (i==NEW_RECORD) {
          int insertResult = flightCrewMemberRepository.insertFlightCrewMember(flightCrewMember);
        }
        else {
          flightCrewMember = flightCrewMemberRepository.save(flightCrewMember);
        }
      }
    }
             
    if ((pilotRareThingShortNames != null && pilotRareThingShortNames.length>0) && pilotRareThingShortNames[0].length()>0 ) {
      PilotRareThing pilotRareThing = pilotRareThingRepository.findOne(pilotId);
      if (pilotRareThing == null) {
        pilotRareThing = new PilotRareThing();
      }
      pilotRareThing.setPilot(pilot);
      pilotRareThing.setShortName(pilotRareThingShortNames[0]);
      if ((pilotRareThingLongNames != null && pilotRareThingLongNames.length>0)) { // && pilotRareThingLongNames[0].length()>0 ) {
        pilotRareThing.setLongName(pilotRareThingLongNames[0]);
      }
      if ((pilotRareThingNotess != null && pilotRareThingNotess.length>0)) { //&& pilotRareThingNotess[0].length()>0 ) {
        pilotRareThing.setNotes(pilotRareThingNotess[0]);
      }
      pilotRareThing=pilotRareThingRepository.save(pilotRareThing);
    }
                 
    return pilot;
  }
  
  
  @Override
  @Transactional
  public int deleteEntity(Integer pilotId) {
    Pilot pilot = pilotRepository.findOne(pilotId);
    try {
      pilotRepository.delete(pilot);
      return 1;
    } catch (Exception e) { // WILL THIS EVER HAPPEN? WHAT HAPPENS IF DELETE FAILS?
      out.println("PilotServiceClass threw Exception: " + e.getMessage());
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  @Transactional
  public int deletePilotCertification 
                  ( Integer pilotCertificationPilotId
                  , Integer pilotCertificationAircraftTypeId
                  ) {
    Pilot pilot = pilotRepository.findOne(pilotCertificationPilotId);
    AircraftType aircraftType = aircraftTypeRepository.findOne(pilotCertificationAircraftTypeId);
    PilotCertification pilotCertification = pilotCertificationRepository.findOneByPilotAndAircraftType(pilot, aircraftType);
    if (pilotCertification != null) { 
      pilotCertificationRepository.delete(pilotCertification);
      pilotCertification = pilotCertificationRepository.findOneByPilotAndAircraftType(pilot, aircraftType);
      if (pilotCertification == null) {
        return 1;
      }
      else {
        return -1;
      }
    }
    else { return -1; }
  }

  @Override
  @Transactional
  public int deleteFlightCrewMember 
                  ( Integer flightCrewMemberFlightId
                  , Integer flightCrewMemberPilotId
                  ) {
    Flight flight = flightRepository.findOne(flightCrewMemberFlightId);
    Pilot pilot = pilotRepository.findOne(flightCrewMemberPilotId);
    FlightCrewMember flightCrewMember = flightCrewMemberRepository.findOneByFlightAndPilot(flight, pilot);
    if (flightCrewMember != null) { 
      flightCrewMemberRepository.delete(flightCrewMember);
      return 1;
    }
    else { return -1; }
  }
  
  
  @Override
  @Transactional
  public int deletePilotRareThing 
                  ( Integer pilotRareThingPilotId
                  ) {
    out.println("");
    out.println("PilotServiceImpl.deletePilotRareThing");
    out.println("");
    PilotRareThing pilotRareThing = pilotRareThingRepository.findOne(pilotRareThingPilotId);
    if (pilotRareThing != null) { 
      pilotRareThingRepository.delete(pilotRareThing);
      pilotRareThing = pilotRareThingRepository.findOne(pilotRareThingPilotId);
      if (pilotRareThing == null) {
        return 1;
      }
      else {
        return -1;
      }
    }
    else { return -1; }
  }
  
  
  public List<Pilot> getQualifiedPilotsByAircraftTypeId (Integer aircraftTypeId){
    return pilotRepository.selectQualifiedPilotsByAircraftTypeId(aircraftTypeId);
  }
  public List<Pilot> getQualifiedPilotsByFlightId (Integer flightId){
    return pilotRepository.selectQualifiedPilotsByFlightId(flightId);
  }
      
}

