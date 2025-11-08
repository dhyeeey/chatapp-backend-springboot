package com.dhyey.chatapp_withjpa.configs;

import com.dhyey.chatapp_withjpa.filters.SessionFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<SessionFilter> filterRegistrationBean() {
        FilterRegistrationBean<SessionFilter> filterRegistrationBean = new FilterRegistrationBean<>();

        filterRegistrationBean.setFilter(new SessionFilter());
        filterRegistrationBean.addUrlPatterns(
                "/api/loadinitialdata",
                "/api/checkconversationexists",
                "/api/users/search",
                "/api/createconversation",
                "/api/fetchprevmessages"
        );
        filterRegistrationBean.setOrder(1);

        return filterRegistrationBean;
    }
}
