package com.chatforyou.io.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "BOARD")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDX", nullable = false)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_IDX", nullable = false)
    private User userIdx;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "CONTENTS", nullable = false)
    private String contents;

    @Column(name = "VIEWS")
    private Integer views;

    @Column(name = "CREATE_DATE", nullable = false)
    private Long createDate;

    @Column(name = "UPDATE_DATE")
    private Long updateDate;

}