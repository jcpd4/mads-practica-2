package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.model.Equipo;
import madstodolist.repository.EquipoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//test 7
import java.util.List;
import java.util.stream.Collectors;

//test 8
import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;




@Service
public class EquipoService {

    @Autowired
    EquipoRepository equipoRepository;
    @Autowired
    UsuarioRepository usuarioRepository;


    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        Equipo equipo = new Equipo(nombre);
        equipoRepository.save(equipo);
        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if (equipo == null) return null;
        return modelMapper.map(equipo, EquipoData.class);
    }
    @Transactional(readOnly = true)
    public List<EquipoData> findAllOrdenadoPorNombre() {
        List<Equipo> equipos = equipoRepository.findAllByOrderByNombreAsc();
        return equipos.stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());
    }
        @Transactional
    public void añadirUsuarioAEquipo(Long idEquipo, Long idUsuario) {
        Equipo equipo = equipoRepository.findById(idEquipo).orElse(null);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        equipo.addUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long idEquipo) {
        Equipo equipo = equipoRepository.findById(idEquipo).orElse(null);
        return equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }

}
