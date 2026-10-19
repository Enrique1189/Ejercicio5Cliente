package com.mx.cbtis.bromitas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.mx.cbtis.controller.GestorController;
import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

@SpringBootTest
@Import(GestorControllerTest.RestTemplateMockConfig.class)
public class GestorControllerTest {

    /**
     * Cliente HTTP prioritario para las pruebas. El controlador debe recibir
     * RestTemplate por inyeccion de dependencias para utilizar este bean.
     */
    @TestConfiguration
    static class RestTemplateMockConfig {
        @Bean("restTemplateParaPruebas")
        @Primary
        RestTemplate restTemplateParaPruebas() {
            return new RestTemplate();
        }
    }


    @Autowired
    private GestorController controller;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setUp() {
        // Intercepta las llamadas realizadas con ESTE RestTemplate.
        // No se inicia un servidor ni se abre una conexion HTTP real.
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void testGetHome() {
        String viewName = controller.getHome();
        assertEquals("home", viewName);
    }

    @Test
    public void testGetTutorial() {
        String viewName = controller.getTutorial();
        assertEquals("tutorial", viewName);
    }

    @Test
    public void testGetManual() {
        String viewName = controller.getManual();
        assertEquals("manual", viewName);
    }

    @Test
    public void testGetLogin() {
        String viewName = controller.getLogin();
        assertEquals("login", viewName);
    }

    @Test
    public void testGetRegister() {
        Model model = new ConcurrentModel();
        String viewName = controller.getRegister(model);

        assertEquals("register", viewName);
        assertNotNull(model.getAttribute("usuario"));
    }

    @Test
    public void testSetSaveUsuario() {
        Usuario usuario = new Usuario();

        mockServer.expect(requestTo("http://localhost:8081/addUsuario"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

        ModelAndView mav = controller.setSaveUsuario(usuario);
        assertEquals("redirect:/login", mav.getViewName());
        mockServer.verify();
    }

    @Test
    public void testGetInicio() {
        Model model = new ConcurrentModel();
        String viewName = controller.getInicio(model);
        assertEquals("inicio", viewName);
        assertNotNull(model.getAttribute("proyectos"));
        mockServer.verify();
    }

    @Test
    public void testSetSaveProyecto() {
        Proyectos proyecto = new Proyectos();

        mockServer.expect(requestTo("http://localhost:8081/addProyecto"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

        ModelAndView mav = controller.setSaveProyecto(proyecto);
        assertEquals("redirect:/inicio", mav.getViewName());
        mockServer.verify();
    }

    @Test
    public void testConsultarProyectos() {
        Model model = new ConcurrentModel();
        String jsonResponse = "[{\"id_proyecto\": 1, \"nombre\": \"Proyecto Test\"}]";

        mockServer.expect(requestTo("http://localhost:8081/buscarProyectos"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = controller.consultarProyectos(model);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(model.getAttribute("listaProyectos"));
        mockServer.verify();
    }

    @Test
    public void testEliminarProyecto() {
        Proyectos proyecto = new Proyectos();
        proyecto.setId_proyecto(1);

        mockServer.expect(requestTo("http://localhost:8081/deleteProyecto"))
                  .andExpect(method(HttpMethod.DELETE))
                  .andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

        ModelAndView mav = controller.eliminarProyecto(proyecto);
        assertEquals("redirect:/inicio", mav.getViewName());
        mockServer.verify();
    }

    @Test
    public void testUpdateProyecto() {
        Proyectos proyecto = new Proyectos();
        proyecto.setId_proyecto(1);

        String jsonResponse = "{\"id_proyecto\": 1, \"nombre\": \"Proyecto Actualizado\"}";

        mockServer.expect(requestTo("http://localhost:8081/buscarProyecto?id_proyecto=1"))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = controller.updateProyecto(proyecto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    public void testVerificarUsuario() {
        Usuario usuario = new Usuario();
        String jsonResponse = "{\"id\": 1, \"nombre\": \"Usuario Test\"}";

        mockServer.expect(requestTo("http://localhost:8081/getUsuarioLogin"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<Usuario> response = controller.verificarUsuario(usuario);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    public void testConsultarUsuarios() {
        Model model = new ConcurrentModel();
        String jsonResponse = "[{\"id\": 1, \"nombre\": \"User1\"}]";

        mockServer.expect(requestTo("http://localhost:8081/buscarUsuarios"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = controller.consultarUsuarios(model);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(model.getAttribute("listaUsuarios"));
        mockServer.verify();
    }

    @Test
    public void testObtenerTareas() {
        Tareas tareaParam = new Tareas();
        Model model = new ConcurrentModel();
        String jsonResponse = "[{\"id_tarea\": 1, \"nombre\": \"Tarea 1\"}]";

        mockServer.expect(requestTo("http://localhost:8081/getTarea"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<List<Tareas>> response = controller.obtenerTareas(tareaParam, model);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(model.getAttribute("tareas"));
        mockServer.verify();
    }

    @Test
    public void testObtenerComentarios() {
        Comentarios comentarioParam = new Comentarios();
        Model model = new ConcurrentModel();
        String jsonResponse = "[{\"id_comentario\": 1, \"texto\": \"Comentario 1\"}]";

        mockServer.expect(requestTo("http://localhost:8081/getComentario"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<List<Comentarios>> response = controller.obtenerComentarios(comentarioParam, model);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(model.getAttribute("comentarios"));
        mockServer.verify();
    }

    @Test
    public void testGetProyecto() {
        Model model = new ConcurrentModel();
        String jsonResponse = "[{\"id\": 1, \"nombre\": \"User1\"}]";

        mockServer.expect(requestTo("http://localhost:8081/buscarUsuarios"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        String viewName = controller.getProyecto(model);

        assertEquals("proyecto", viewName);
        assertNotNull(model.getAttribute("tarea"));
        assertNotNull(model.getAttribute("comentarios"));
        assertNotNull(model.getAttribute("listaUsuarios"));
        mockServer.verify();
    }

    @Test
    public void testGetTarea() {
        Model model = new ConcurrentModel();
        String viewName = controller.getTarea(model);

        assertEquals("proyecto", viewName);
        assertNotNull(model.getAttribute("tareas"));
    }

    @Test
    public void testSetSaveTareas() {
        Tareas tareas = new Tareas();
        tareas.setId_proyecto(5);

        mockServer.expect(requestTo("http://localhost:8081/addTareas"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

        ModelAndView mav = controller.setSaveTareas(tareas);

        assertEquals("redirect:/proyecto", mav.getViewName());
        assertEquals(5, mav.getModel().get("id_proyecto"));
        mockServer.verify();
    }

    @Test
    public void testSetSaveComentario() {
        Comentarios comentario = new Comentarios();

        mockServer.expect(requestTo("http://localhost:8081/addComentario"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

        String viewName = controller.setSaveComentario(comentario);
        assertEquals("proyecto", viewName);
        mockServer.verify();
    }

    @Test
    public void testUpdateTarea() {
        Tareas tarea = new Tareas();
        tarea.setId_tarea(10);

        String jsonResponse = "{\"id_tarea\": 10, \"nombre\": \"Tarea Actualizada\"}";

        mockServer.expect(requestTo("http://localhost:8081/buscarTarea?id_tarea=10"))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = controller.updateTarea(tarea);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    public void testEliminarTarea() {
        Tareas tarea = new Tareas();
        tarea.setId_tarea(10);

        mockServer.expect(requestTo("http://localhost:8081/deleteTareas"))
                  .andExpect(method(HttpMethod.DELETE))
                  .andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

        ModelAndView mav = controller.eliminarTarea(tarea);
        assertEquals("redirect:/proyecto", mav.getViewName());
        mockServer.verify();
    }

    @Test
    public void testVerificarTarea() {
        Tareas tarea = new Tareas();

        mockServer.expect(requestTo("http://localhost:8081/addTareas"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

        ResponseEntity<Integer> response = controller.verificarTarea(tarea);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    public void testCambiosTareas() {
        Tareas tarea = new Tareas();

        mockServer.expect(requestTo("http://localhost:8081/addTareas"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

        ResponseEntity<Integer> response = controller.cambiosTareas(tarea);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    public void testConsultarTareas() {
        String jsonResponse = "[{\"id_tarea\": 1, \"nombre\": \"Tarea General\"}]";

        mockServer.expect(requestTo("http://localhost:8081/buscarTareas"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = controller.consultarTareas();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    public void testConsultarComentarios() {
        String jsonResponse = "[{\"id_comentario\": 1, \"texto\": \"Comentario General\"}]";

        mockServer.expect(requestTo("http://localhost:8081/buscarComentarios"))
                  .andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = controller.consultarComentarios();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }
}