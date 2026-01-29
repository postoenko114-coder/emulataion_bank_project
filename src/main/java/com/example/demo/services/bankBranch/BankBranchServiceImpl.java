package com.example.demo.services.bankBranch;

import com.example.demo.dto.*;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.models.branch.Location;
import com.example.demo.models.branch.WorkingHour;
import com.example.demo.repositories.BankBranchRepository;
import com.example.demo.repositories.BankServiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BankBranchServiceImpl implements BankBranchService {
    @Autowired
    private BankBranchRepository bankBranchRepository;
    @Autowired
    private BankServiceRepository bankServiceRepository;

    public BankBranchServiceImpl(BankBranchRepository bankBranchRepository, BankServiceRepository bankServiceRepository) {
        this.bankBranchRepository = bankBranchRepository;
        this.bankServiceRepository = bankServiceRepository;
    }

    @Transactional
    @Override
    public BankBranchDTO addBankBranch(BankBranchDTO bankBranchDTO, LocationDTO locationDTO) {
        BankBranch bankBranch = new BankBranch();
        Location location = new Location();
        location.setCity(locationDTO.getCity());
        location.setCountry(locationDTO.getCountry());
        location.setAddress(locationDTO.getAddress());
        location.setPostCode(locationDTO.getPostCode());

        if (locationDTO.getLatitude() != null && locationDTO.getLongitude() != null) {
            location.setLatitude(locationDTO.getLatitude());
            location.setLongitude(locationDTO.getLongitude());
        } else {
            enrichWithCoordinates(location);
        }

        if (bankBranch.getSchedule() == null) {
            bankBranch.setSchedule(new HashSet<>());
        }
        if(bankBranch.getBankServices() == null) {
            bankBranch.setServices(new HashSet<>());
        }
        bankBranch.setBankBranchName(bankBranchDTO.getBankBranchName());
        bankBranch.setLocation(location);
        bankBranchRepository.save(bankBranch);
        return bankBranch.toDTO();
    }

    @Transactional
    @Override
    public BankBranchDTO updateBankBranch(Long bankBranchId, BankBranchDTO bankBranchDTO) {
        BankBranch bankBranch = bankBranchRepository.findById(bankBranchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Branch not found"));

        if (bankBranchDTO.getBankBranchName() != null) {
            bankBranch.setBankBranchName(bankBranchDTO.getBankBranchName());
        }

        LocationDTO locDTO = bankBranchDTO.getLocationDTO();
        if (locDTO != null) {
            Location location = bankBranch.getLocation();
            location.setCountry(locDTO.getCountry());
            location.setCity(locDTO.getCity());
            location.setAddress(locDTO.getAddress());
            location.setPostCode(locDTO.getPostCode());

            if (locDTO.getLatitude() != null) location.setLatitude(locDTO.getLatitude());
            if (locDTO.getLongitude() != null) location.setLongitude(locDTO.getLongitude());
        }

        bankBranch.getSchedule().clear();
        if (bankBranchDTO.getSchedule() != null) {
            for (WorkingHourDTO whDTO : bankBranchDTO.getSchedule()) {
                WorkingHour wh = new WorkingHour();
                wh.setDayOfWeek(DayOfWeek.valueOf(whDTO.getDayOfWeek())); // String -> Enum
                wh.setOpenTime(LocalTime.parse(whDTO.getOpenTime()));
                wh.setCloseTime(LocalTime.parse(whDTO.getCloseTime()));
                bankBranch.getSchedule().add(wh);
            }
        }

        if (bankBranchDTO.getBankServices() != null) {
            List<Long> serviceIds = bankBranchDTO.getBankServices().stream()
                    .map(BankServiceDTO::getId)
                    .toList();
            List<BankService> foundServices = bankServiceRepository.findAllById(serviceIds);
            bankBranch.setServices(new HashSet<>(foundServices));
        }

        BankBranch saved = bankBranchRepository.save(bankBranch);
        return saved.toDTO();
    }

    @Transactional
    @Override
    public void deleteBankBranch(Long bankBranch_id){
        bankBranchRepository.deleteById(bankBranch_id);
    }

    @Transactional
    @Override
    public BankBranchDTO getBankBranchById(Long bankBranch_id){
        BankBranch bankBranch = bankBranchRepository.findById(bankBranch_id).orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND , "Bank Branch not found"));
        return bankBranch.toDTO();
    }

    @Transactional
    @Override
    public List<BankBranchDTO> getAllBankBranches(){
        List<BankBranch> bankBranches = bankBranchRepository.findAll();
        return getBankBranchDTOs(bankBranches);
    }

    @Transactional
    @Override
    public List<BankBranchDTO> getBranchesByService(String serviceName){
        return getBankBranchDTOs(bankBranchRepository.findByServiceName(serviceName));
    }

    @Transactional
    @Override
    public List<BankBranchDTO> getBranchesByLocation(String city, String street){
        List<BankBranch> bankBranches = bankBranchRepository.searchByCityAndStreetPartially(city, street);
        return getBankBranchDTOs(bankBranches);
    }

    @Transactional
    @Override
    public List<BankBranchDTO> getNearestBranches(Double userLat, Double userLot){
        List<BankBranch> bankBranches = bankBranchRepository.findNearestBranchesNative(userLat, userLot);
        List<BankBranchDTO> bankBranchDTOs = new ArrayList<>();
        for(BankBranch bankBranch : bankBranches){
            bankBranchDTOs.add(bankBranch.toDTO());
        }
        return getBankBranchDTOs(bankBranches);
    }

    @Transactional
    @Override
    public void addBankServiceToBranch(Long bankBranch_id, Long bankService_id){
        BankService bankService = bankServiceRepository.findById(bankService_id).orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND , "Service not found"));

        BankBranch bankBranch = bankBranchRepository.findById(bankBranch_id).orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND , "Bank Branch not found"));

        bankBranch.getBankServices().add(bankService);
        bankService.getBankBranches().add(bankBranch);
    }

    @Transactional
    @Override
    public void deleteBankServiceFromBranch(Long bankBranch_id, Long bankService_id){
        BankService bankService = bankServiceRepository.findById(bankService_id).orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND , "Service not found"));

        BankBranch bankBranch = bankBranchRepository.findById(bankBranch_id).orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND , "Bank Branch not found"));

        bankBranch.getBankServices().remove(bankService);
        bankService.getBankBranches().remove(bankBranch);
    }

    @Transactional
    @Override
    public List<BankServiceDTO> getBankServicesOfBranch(Long bankBranch_id){
        BankBranch bankBranch = bankBranchRepository.findById(bankBranch_id).orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND , "Service not found"));
        Set<BankService> bankServices = bankBranch.getBankServices();
        List<BankServiceDTO> bankServiceDTOs = new ArrayList<>();
        for(BankService bankService : bankServices){
            bankServiceDTOs.add(bankService.toDTO());
        }
        return bankServiceDTOs;
    }

    @Transactional
    @Override
    public boolean isBranchOpen(Long bankBranch_id, LocalDateTime dateTime) {
        BankBranch branch = bankBranchRepository.findById(bankBranch_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Branch not found"));

        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        LocalTime checkTime = dateTime.toLocalTime();

        return branch.getSchedule().stream()
                .filter(wh -> wh.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .map(wh -> {
                    boolean isOpen = checkTime.isAfter(wh.getOpenTime()) && checkTime.isBefore(wh.getCloseTime());
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    @Override
    public BankBranch findBranchByName(String branchName){
        return bankBranchRepository.findByName(branchName).orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND , "Bank Branch not found"));
    }

    private List<BankBranchDTO> getBankBranchDTOs(List<BankBranch> bankBranches){
        List<BankBranchDTO> bankBranchDTOs = new ArrayList<>();
        for (BankBranch bankBranch : bankBranches) {
            bankBranchDTOs.add(bankBranch.toDTO());
        }
        return bankBranchDTOs;
    }

    private void enrichWithCoordinates(Location location) {
        try {
            String query = location.getCity() + ", " + location.getAddress();
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String url = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&limit=1";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "BankAppAdmin/1.0")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());

                if (root.isArray() && !root.isEmpty()) {
                    JsonNode firstResult = root.get(0);
                    location.setLatitude(firstResult.get("lat").asDouble());
                    location.setLongitude(firstResult.get("lon").asDouble());
                }
            }
        } catch (Exception e) {

            System.err.println("Geocoding failed: " + e.getMessage());
        }
    }

}
