package com.nikunj.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nikunj.library.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

}