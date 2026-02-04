package com.example.demo;

import com.example.demo.models.account.Account;
import com.example.demo.models.account.CurrencyAccount;
import com.example.demo.models.account.StatusAccount;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.models.branch.Location;
import com.example.demo.models.branch.WorkingHour;
import com.example.demo.models.user.RoleUser;
import com.example.demo.models.user.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.BankBranchRepository;
import com.example.demo.repositories.BankServiceRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner init(AccountRepository accountRepository, BankBranchRepository bankBranchRepository, UserRepository userRepository, BankServiceRepository bankServiceRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            User user1 = new User("Sasha", passwordEncoder.encode("12345"), "post213@djfjkd.com", RoleUser.ADMIN);
            User user2 = new User("Oleg", passwordEncoder.encode("54321"), "oleg213@djfjkd.com", RoleUser.CLIENT);
            userRepository.save(user1);
            userRepository.save(user2);

            Account account1 = new Account("1111111/1111", CurrencyAccount.CZK, new BigDecimal(2000), StatusAccount.ACTIVE, user1);
            Account account2 = new Account("2222222/2222", CurrencyAccount.USD, new BigDecimal(1000), StatusAccount.ACTIVE, user2);

            accountRepository.save(account1);
            accountRepository.save(account2);

            Location location = new Location("Prague", "Street 1" , "Czech Republic", 50.075, 14.437 , "11111");

            BankService bankService = new BankService("Lending", "30 minutes", "Lending consultation");
            bankServiceRepository.save(bankService);
            BankBranch bankBranch =  new BankBranch("Branch 1", location);

            Set<BankService> bankServices = new HashSet<>();
            bankServices.add(bankService);
            bankBranch.setServices(bankServices);
            bankBranch.setSchedule(getClassicSchedule());

            Set<BankBranch> bankBranches = new HashSet<>();
            bankBranches.add(bankBranch);
            bankService.setBankBranches(bankBranches);

            bankBranchRepository.save(bankBranch);
            bankServiceRepository.save(bankService);

        };
    }

    public static Set<WorkingHour> getClassicSchedule() {
        Set<WorkingHour> schedule = new HashSet<>();

        LocalTime opening = LocalTime.of(8, 0);  // 08:00
        LocalTime closing = LocalTime.of(17, 0); // 17:00

        for (DayOfWeek day : DayOfWeek.values()) {
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                schedule.add(new WorkingHour(day, opening, closing));
            }
        }
        return schedule;
    }

}
