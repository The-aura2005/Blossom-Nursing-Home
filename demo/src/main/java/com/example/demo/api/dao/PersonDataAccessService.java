package com.example.demo.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.demo.model.Person;


@Repository("sql")
public class PersonDataAccessService implements PersonDao {
    @Override
    public int insertPerson(UUID id, Person person){
        return 0;
    }
    @Override
    public List<Person> selectAllpeople(){
        return List.of(new Person(UUID.randomUUID(),"FROM SQL DB"));
    }
    @Override
    public Optional<Person> selectPersonById(UUID id){
        return Optional.empty();
    }
    @Override
    public int deletePersonById(UUID id){
        return 0;
    }
    @Override
    public int updatePersonById(UUID id, Person person){
        return 0;
    }

    
}