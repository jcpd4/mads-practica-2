package madstodolist.service;

import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioService {

    Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public enum LoginStatus {LOGIN_OK, USER_NOT_FOUND, ERROR_PASSWORD, USER_BLOCKED}

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public LoginStatus login(String eMail, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(eMail);
        if (!usuario.isPresent()) {
            return LoginStatus.USER_NOT_FOUND;
        } else if (!usuario.get().getPassword().equals(password)) {
            return LoginStatus.ERROR_PASSWORD;
        } else if (usuario.get().isBloqueado()) {
            return LoginStatus.USER_BLOCKED;
        } else {
            return LoginStatus.LOGIN_OK;
        }
    }

    // Se añade un usuario en la aplicación.
    // El email y password del usuario deben ser distinto de null
    // El email no debe estar registrado en la base de datos
    @Transactional
    public UsuarioData registrar(UsuarioData usuario) {
        Optional<Usuario> usuarioBD = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuario.isAdmin() && existeAdmin()) {
            throw new UsuarioServiceException("Ya existe un usuario administrador");
        }
        if (usuarioBD.isPresent())
            throw new UsuarioServiceException("El usuario " + usuario.getEmail() + " ya está registrado");
        else if (usuario.getEmail() == null)
            throw new UsuarioServiceException("El usuario no tiene email");
        else if (usuario.getPassword() == null)
            throw new UsuarioServiceException("El usuario no tiene password");
        else {
            Usuario usuarioNuevo = modelMapper.map(usuario, Usuario.class);
            usuarioNuevo = usuarioRepository.save(usuarioNuevo);
            return modelMapper.map(usuarioNuevo, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findById(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }
    @Transactional(readOnly = true)
    public List<UsuarioData> allUsuarios() {
        List<UsuarioData> usuarios = new ArrayList<>();
        for (Usuario usuario : usuarioRepository.findAll()) {
            usuarios.add(modelMapper.map(usuario, UsuarioData.class));
        }
        return usuarios;
    }
    @Transactional(readOnly = true)
    public boolean existeAdmin() {
        return usuarioRepository.existsByAdminTrue();
    }

    @Transactional
    public UsuarioData cambiarBloqueo(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        
        if (usuario == null) {
            throw new UsuarioServiceException("El usuario no existe");
        }
        if (usuario.isAdmin()) {
            throw new UsuarioServiceException("No se puede bloquear al administrador");
        }
        
        // Invertimos el valor (si era false pasa a true, y viceversa)
        usuario.setBloqueado(!usuario.isBloqueado());
        
        usuario = usuarioRepository.save(usuario);
        return modelMapper.map(usuario, UsuarioData.class);
    }
}
