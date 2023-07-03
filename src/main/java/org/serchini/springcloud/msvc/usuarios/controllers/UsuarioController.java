package org.serchini.springcloud.msvc.usuarios.controllers;

import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.serchini.springcloud.msvc.usuarios.models.entity.Usuario;
import org.serchini.springcloud.msvc.usuarios.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> listar(){
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id) {
        Optional<Usuario> usuarioOptional = usuarioService.porId(id);
        if (usuarioOptional.isPresent()){
            return ResponseEntity.ok(usuarioOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Usuario usuario, BindingResult result){

        if(result.hasErrors()) {
            return validar(result);
        }
        if (!usuario.getEmail().isEmpty() && usuarioService.existePorEmail(usuario.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Collections
                            .singletonMap("mensaje", "Ya existe un usuario con ese email"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@Valid @RequestBody Usuario usuario, BindingResult result, @PathVariable Long id) {
        if(result.hasErrors()) {
            return validar(result);
        }
        Optional<Usuario> usuarioOptional = usuarioService.porId(id);
        if(usuarioOptional.isPresent()) {
            Usuario usuarioDb = usuarioOptional.get();

            if (!usuario.getEmail().isEmpty() &&
                    !usuario.getEmail().equalsIgnoreCase(usuarioDb.getEmail()) &&
                    usuarioService.porEmail(usuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Collections
                                .singletonMap("mensaje", "Ya existe un usuario con ese email"));
            }

            usuarioDb.setNombre(usuario.getNombre());
            usuarioDb.setEmail(usuario.getEmail());
            usuarioDb.setPassword(usuario.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(usuarioDb));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<Usuario> usuarioOptional = usuarioService.porId(id);
        if (usuarioOptional.isPresent()) {
            usuarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private static ResponseEntity<Map<String, String>> validar(BindingResult result) {
        Map<String, String> errores = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errores.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errores);
    }
}
