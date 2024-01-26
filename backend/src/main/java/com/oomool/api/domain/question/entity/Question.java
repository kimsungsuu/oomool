package com.oomool.api.domain.question.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "question")
public class Question {

    @Id
    private int id; // pk
    private String question; // 질문
    private int level; // 질문 깊이

}