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
import org.springframework.http.HttpMethod;

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
                                                .requestMatchers("/loginn", "/login", "/dashboard-redirect",
                                                                "/loginn.html",
                                                                "/login-error.html")
                                                .permitAll()

                                                // Staff pages - requires ROLE_STAFF
                                                .requestMatchers("/staff-dashboard", "/staff-dashboard.html",
                                                                "/MyAssignedResidents", "/MyAssignedResidents.html",
                                                                "/MyTask", "/MyTask.html", "/MyTask/complete",
                                                                "/VitalLoggingTable",
                                                                "/activitiesLogging", "/activitiesLogging.html",
                                                                "/ActivityLogging", "/activitiesLogging/add",
                                                                "/medications/new/**", "/medications/save",
                                                                "/inventory/issue/new/**", "/inventory/issue/save",
                                                                "/reports/new/**", "/reports/save",
                                                                "/medications", "/incident-reports")
                                                .hasRole("STAFF")

                                                // Shared staff/admin logging routes
                                                .requestMatchers("/VitalLogging", "/VitalLogging.html", "/vitals")
                                                .hasAnyRole("STAFF", "ADMIN")

                                                // Admin dashboard - requires ROLE_ADMIN
                                                .requestMatchers("/admin-dashboard", "/admin-dashboard.html",
                                                                "/residents", "/residents.html", "/editResident",
                                                                "/editResident.html", "/addResident",
                                                                "/addResident.html", "/staffs", "/staffs.html",
                                                                "/addStaff", "/addStaff.html", "/assign-task",
                                                                "/medical", "/medical/**",
                                                                "/admin/VitalLoggingTable",
                                                                "/admin/activitiesLogging",
                                                                "/admin/medications",
                                                                "/admin/incident-reports")
                                                .hasRole("ADMIN")

                                                .requestMatchers("/accountant-dashboard", "/accountant-dashboard.html",
                                                                "/resident-payments",
                                                                "/resident-payments.html",
                                                                "/staff-payments", "/staff-payments.html",
                                                                "/report-payments", "/report-payments.html",
                                                                "/inventory-payments", "/inventory-payments.html")
                                                .hasRole("ACCOUNTANT")
                                                .requestMatchers("/inventory-manager", "/inventory-manager.html")
                                                .hasRole("INVENTORY_MANAGER")

                                                .requestMatchers(HttpMethod.GET, "/api/items/available")
                                                .hasRole("STAFF")
                                                .requestMatchers(HttpMethod.POST, "/api/items/issue")
                                                .hasRole("STAFF")
                                                .requestMatchers("/api/items/**", "/api/suppliers/**", "/api/logs/**")
                                                .hasRole("INVENTORY_MANAGER")

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