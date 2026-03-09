package nursing_home.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private UserDetailsService userDetailsService;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
                httpSecurity.csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // Permit public access to index page and static resources
                                                .requestMatchers("/", "/index.html", "/contact", "/contact.html",
                                                                "/static/**", "/css/**", "/js/**",
                                                                "/images/**")
                                                .permitAll()
                                                .requestMatchers("/loginn","/login","/dashboard-redirect", "/loginn.html",
                                                                "/login-error.html")
                                                .permitAll()

                                                // Billing reports - shared between ADMIN and ACCOUNTANT
                                                .requestMatchers("/billingreports", "/billingreports.html")
                                                .hasAnyRole("ADMIN", "ACCOUNTANT")

                                                // Staff dashboard - requires ROLE_STAFF
                                                .requestMatchers("/staff-dashboard", "/staff-dashboard.html",
                                                                "/MyAssignedResidents", "/MyAssignedResidents.html",
                                                                "/MyTask", "/MyTask.html", "/activitiesLogging",
                                                                "/activitiesLogging.html", "/MyTask/complete", "/VitalLogging",
                                                                "/VitalLogging.html", "/VitalLoggingTable", "/vitals",
                                                                "/activitiesLogging")
                                                .hasRole("STAFF")

                                                // Admin dashboard - requires ROLE_ADMIN
                                                .requestMatchers("/admin-dashboard", "/admin-dashboard.html",
                                                                "/residents", "/residents.html", "/editResident",
                                                                "/editResident.html", "/addResident",
                                                                "/addResident.html", "/staffs", "/staffs.html",
                                                                "/addStaff", "/addStaff.html", "/assign-task",
                                                                "/medical", "/medical/**")
                                                .hasRole("ADMIN")

                                                // Accountant dashboard - requires ROLE_ACCOUNTANT
                                                .requestMatchers("/accountant-dashboard", "/accountant-dashboard.html",
                                                                "/billing/**")
                                                .hasRole("ACCOUNTANT")

                                                // All other requests require authentication
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/loginn")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/dashboard-redirect", true)
                                                .failureUrl("/loginn?error")
                                                .permitAll());

                return httpSecurity.build();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return provider;
        }
}