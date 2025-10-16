package com.quetoquenana.personservice;

import com.quetoquenana.personservice.controller.PersonController;
import com.quetoquenana.personservice.controller.ProfileController;
import com.quetoquenana.personservice.service.ProfileService;
import com.quetoquenana.personservice.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest({PersonController.class, ProfileController.class})
class PersonServiceApplicationTests {

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
