package com.alex.bank.services.bankBranch;

import com.alex.bank.dto.BankBranchDTO;
import com.alex.bank.dto.BankServiceDTO;
import com.alex.bank.dto.LocationDTO;
import com.alex.bank.dto.WorkingHourDTO;
import com.alex.bank.mapper.BankBranchMapper;
import com.alex.bank.mapper.BankServiceMapper;
import com.alex.bank.models.branch.BankBranch;
import com.alex.bank.models.branch.BankService;
import com.alex.bank.models.branch.Location;
import com.alex.bank.models.branch.WorkingHour;
import com.alex.bank.repositories.BankBranchRepository;
import com.alex.bank.repositories.BankServiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankBranchServiceImpl implements BankBranchService {

    private final BankBranchRepository bankBranchRepository;

    private final BankServiceRepository bankServiceRepository;

    private final BankBranchMapper bankBranchMapper;

    private final BankServiceMapper bankServiceMapper;

    @Transactional
    @Override
    public BankBranchDTO addBankBranch(BankBranchDTO bankBranchDTO, LocationDTO locationDTO) {
        BankBranch bankBranch = bankBranchMapper.toEntity(bankBranchDTO, locationDTO);

        Location location = bankBranch.getLocation();
        if (location.getLatitude() == null || location.getLongitude() == null) {
            enrichWithCoordinates(location);
        }

        BankBranch saved = bankBranchRepository.save(bankBranch);
        log.info("Bank branch created branchId={} name={}", saved.getId(), saved.getBankBranchName());
        return bankBranchMapper.toDTO(saved);
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

        if (bankBranchDTO.getSchedule() != null) {
            bankBranch.getSchedule().clear();
            for (WorkingHourDTO whDTO : bankBranchDTO.getSchedule()) {
                WorkingHour wh = new WorkingHour();
                wh.setDay(parseDayOfWeek(whDTO.getDayOfWeek()));
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
            bankBranch.setBankServices(new HashSet<>(foundServices));
        }

        BankBranch saved = bankBranchRepository.save(bankBranch);
        log.info("Bank branch updated branchId={} name={}", saved.getId(), saved.getBankBranchName());
        return bankBranchMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public BankBranchDTO getBankBranchById(Long bankBranch_id) {
        BankBranch bankBranch = bankBranchRepository.findById(bankBranch_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Branch not found"));
        return bankBranchMapper.toDTO(bankBranch);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BankBranchDTO> getAllBankBranches() {
        List<BankBranch> bankBranches = bankBranchRepository.findAll();
        return getBankBranchDTOs(bankBranches);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BankBranchDTO> getBranchesByService(String serviceName) {
        if (serviceName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service Name is null");
        }
        return getBankBranchDTOs(bankBranchRepository.findByServiceName(serviceName));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BankBranchDTO> getBranchesByLocation(String city, String street) {
        if (city == null && street == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City and Street are both null");
        }
        List<BankBranch> bankBranches = bankBranchRepository.searchByCityAndStreetPartially(city, street);
        return getBankBranchDTOs(bankBranches);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BankBranchDTO> getNearestBranches(Double userLat, Double userLot) {
        if (userLat == null || userLot == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "We dont have your coordinates");
        }
        List<BankBranch> bankBranches = bankBranchRepository.findNearestBranchesNative(userLat, userLot);
        return getBankBranchDTOs(bankBranches);
    }

    @Transactional
    @Override
    public BankBranchDTO addBankServiceToBranch(Long bankBranch_id, Long bankService_id) {
        BankService bankService = bankServiceRepository.findById(bankService_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        BankBranch bankBranch = bankBranchRepository.findById(bankBranch_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));

        bankBranch.getBankServices().add(bankService);
        bankService.getBankBranches().add(bankBranch);

        bankBranchRepository.save(bankBranch);
        bankServiceRepository.save(bankService);
        log.info("Bank service attached to branch branchId={} serviceId={}", bankBranch_id, bankService_id);
        return bankBranchMapper.toDTO(bankBranch);

    }

    @Transactional
    @Override
    public void deleteBankServiceFromBranch(Long bankBranch_id, Long bankService_id) {
        BankService bankService = bankServiceRepository.findById(bankService_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        BankBranch bankBranch = bankBranchRepository.findById(bankBranch_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));

        bankService.getBankBranches().remove(bankBranch);
        bankBranch.getBankServices().remove(bankService);

        bankBranchRepository.save(bankBranch);
        bankServiceRepository.save(bankService);
        log.info("Bank service removed from branch branchId={} serviceId={}", bankBranch_id, bankService_id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BankServiceDTO> getBankServicesOfBranch(Long bankBranch_id) {
        BankBranch bankBranch = bankBranchRepository.findById(bankBranch_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
        return bankBranch.getBankServices().stream()
                .map(bankServiceMapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public boolean isBranchOpen(Long bankBranch_id, LocalDateTime dateTime) {
        BankBranch branch = bankBranchRepository.findById(bankBranch_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Branch not found"));

        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        LocalTime checkTime = dateTime.toLocalTime();

        return branch.getSchedule().stream()
                .filter(wh -> wh.getDay() == dayOfWeek)
                .findFirst()
                .map(wh -> {
                    boolean isOpen = checkTime.isAfter(wh.getOpenTime()) && checkTime.isBefore(wh.getCloseTime());
                    return isOpen;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    @Override
    public BankBranch findBranchByName(String branchName) {
        return bankBranchRepository.findByName(branchName).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Branch not found"));
    }

    @Transactional
    @Override
    public void deleteBankBranch(Long bankBranch_id) {
        BankBranch bankBranch = bankBranchRepository.findById(bankBranch_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Branch not found"));
        bankBranchRepository.delete(bankBranch);
        log.info("Bank branch deleted branchId={}", bankBranch_id);
    }

    private List<BankBranchDTO> getBankBranchDTOs(List<BankBranch> bankBranches) {
        return bankBranches.stream()
                .map(bankBranchMapper::toDTO)
                .toList();
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
            log.warn("Geocoding failed city={} address={} error={}", location.getCity(), location.getAddress(), e.getMessage());
        }
    }

    private DayOfWeek parseDayOfWeek(String dayOfWeek) {
        try {
            return DayOfWeek.valueOf(dayOfWeek.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid day of week");
        }
    }

}
