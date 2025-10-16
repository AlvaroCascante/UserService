package com.quetoquenana.userservice;

import com.quetoquenana.userservice.controller.PersonController;
import com.quetoquenana.userservice.controller.ProfileController;
import com.quetoquenana.userservice.service.ProfileService;
import com.quetoquenana.userservice.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest({PersonController.class, ProfileController.class})
class UserServiceApplicationTests {

    @MockBean
    private PersonService personService;

    @MockBean
    private ProfileService profileService;

    @Autowired
    private PersonController personController;

    @Autowired
    private ProfileController profileController;

	@Test
	void contextLoads() {
        assertThat(personController).isNotNull();
        assertThat(profileController).isNotNull();
	}

}
