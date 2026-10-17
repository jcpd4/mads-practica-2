package madstodolist.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
// test 4
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "equipos")
public class Equipo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    // Relación LAZY: al recuperar un equipo NO se recuperan automáticamente
    // sus usuarios. Solo se cargan cuando se accede a la colección.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "equipo_usuario",
            joinColumns = { @JoinColumn(name = "fk_equipo") },
            inverseJoinColumns = { @JoinColumn(name = "fk_usuario") })
    Set<Usuario> usuarios = new HashSet<>();


    // Constructor vacío necesario para JPA/Hibernate.
    // No debe usarse desde la aplicación.
    public Equipo() {}

    public Equipo(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }


    public String getNombre() {
        return nombre;
    }
        public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    public void addUsuario(Usuario usuario) {
        // Hay que actualizar ambas colecciones, porque
        // JPA/Hibernate no lo hace automáticamente
        this.getUsuarios().add(usuario);
        usuario.getEquipos().add(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipo equipo = (Equipo) o;
        if (id != null && equipo.id != null)
            // Si tenemos los ID, comparamos por ID
            return Objects.equals(id, equipo.id);
        // si no comparamos por campos obligatorios
        return nombre.equals(equipo.nombre);
    }

    @Override
    public int hashCode() {
        // Generamos un hash basado en los campos obligatorios
        return Objects.hash(nombre);
    }

}
