package com.bip.ejb;

import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "BENEFICIO")
public class Beneficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Setter(AccessLevel.NONE)
    @Column(name = "VALOR", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;

    @Column(name = "ATIVO", nullable = false)
    private boolean ativo = true;

    @Setter(AccessLevel.NONE)
    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    public Beneficio(String nome, String descricao, BigDecimal valor, boolean ativo) {
        this.nome = nome;
        this.descricao = descricao;
        this.valor = Objects.requireNonNull(valor, "valor");
        this.ativo = ativo;
    }

    public void setValor(BigDecimal valor) {
        this.valor = Objects.requireNonNull(valor, "valor");
    }
}
