package com.moodify.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        return  modelMapper;
    }
}



/**
 Configuration Settings
 1. MatchingStrategies.STRICT
     Uses strict matching rules - only maps fields with identical names
     Example: Only maps User.name to UserDTO.name, not User.fullName
     Provides predictable, safe mapping without surprises

 2. setFieldMatchingEnabled(true)
     Allows mapping to work directly with fields, not just getters/setters
     Enables mapping of private fields without requiring public accessors

 3. setSkipNullEnabled(true)
    When true, null values in the source object won't overwrite non-null values in the destination object
    Prevents accidental data loss during updates

 4. setFieldAccessLevel(AccessLevel.PRIVATE)
     Allows ModelMapper to access and modify private fields directly
     Works with fieldMatchingEnabled to map fields without requiring public getters/setters
 * **/
