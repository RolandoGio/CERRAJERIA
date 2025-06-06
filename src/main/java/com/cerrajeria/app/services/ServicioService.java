package com.cerrajeria.app.services;

import com.cerrajeria.app.dao.ServicioDAO;
import com.cerrajeria.app.dao.CategoriaServicioDAO;
import com.cerrajeria.app.models.Servicio;
import com.cerrajeria.app.models.CategoriaServicio;

import java.math.BigDecimal;
import java.util.List;

/**
 * Clase de servicio para la gestión de servicios.
 * Contiene la lógica de negocio para crear, editar, visualizar y desactivar servicios.
 */
public class ServicioService {

    private ServicioDAO servicioDAO;
    private CategoriaServicioDAO categoriaServicioDAO; // Para verificar la existencia de categorías

    // Constructor
    public ServicioService() {
        this.servicioDAO = new ServicioDAO();
        this.categoriaServicioDAO = new CategoriaServicioDAO();
    }

    /**
     * Crea un nuevo servicio con validaciones.
     * @param nombre Nombre del servicio.
     * @param descripcion Descripción del servicio.
     * @param precio Precio de referencia del servicio.
     * @param idCategoriaServicio ID de la categoría a la que pertenece el servicio.
     * @return true si el servicio fue creado exitosamente, false en caso contrario.
     */
    public boolean crearServicio(String nombre, String descripcion, BigDecimal precio, int idCategoriaServicio) {
        // Validaciones básicas
        if (nombre == null || nombre.trim().isEmpty() || precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Error al crear servicio: Campos obligatorios o valores inválidos.");
            return false;
        }

        // Verificar si la categoría existe y está activa
        CategoriaServicio categoria = categoriaServicioDAO.obtenerCategoriaServicioPorId(idCategoriaServicio);
        if (categoria == null || !categoria.isActivo()) {
            System.err.println("Error al crear servicio: La categoría de servicio especificada no existe o está inactiva.");
            return false;
        }

        Servicio nuevoServicio = new Servicio(nombre, descripcion, precio, idCategoriaServicio);
        int id = servicioDAO.insertarServicio(nuevoServicio);
        return id != -1;
    }

    /**
     * Actualiza los datos de un servicio existente.
     * @param servicio El objeto Servicio con los datos actualizados.
     * @return true si la actualización es exitosa, false en caso contrario.
     */
    public boolean actualizarServicio(Servicio servicio) {
        if (servicio == null || servicio.getIdServicio() <= 0) {
            System.err.println("Error al actualizar servicio: ID de servicio inválido.");
            return false;
        }
        if (servicio.getNombre() == null || servicio.getNombre().trim().isEmpty() ||
                servicio.getPrecio() == null || servicio.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Error al actualizar servicio: Campos obligatorios o valores inválidos.");
            return false;
        }

        // Verificar si la categoría existe y está activa (si se cambió)
        CategoriaServicio categoria = categoriaServicioDAO.obtenerCategoriaServicioPorId(servicio.getIdCategoriaServicio());
        if (categoria == null || !categoria.isActivo()) {
            System.err.println("Error al actualizar servicio: La categoría de servicio especificada no existe o está inactiva.");
            return false;
        }

        return servicioDAO.actualizarServicio(servicio);
    }

    /**
     * Desactiva lógicamente un servicio.
     * @param idServicio ID del servicio a desactivar.
     * @return true si la desactivación es exitosa, false en caso contrario.
     */
    public boolean desactivarServicio(int idServicio) {
        Servicio servicio = servicioDAO.obtenerServicioPorId(idServicio);
        if (servicio != null && "Otros".equalsIgnoreCase(servicio.getNombre())) { // Si "Otros" es un servicio especial
            System.err.println("No se puede desactivar el servicio 'Otros'.");
            return false;
        }
        return servicioDAO.desactivarServicio(idServicio);
    }

    /**
     * Activa lógicamente un servicio.
     * @param idServicio ID del servicio a activar.
     * @return true si la activación es exitosa, false en caso contrario.
     */
    public boolean activarServicio(int idServicio) {
        return servicioDAO.activarServicio(idServicio);
    }

    /**
     * Obtiene un servicio por su ID.
     * @param idServicio ID del servicio.
     * @return El objeto Servicio o null.
     */
    public Servicio obtenerServicioPorId(int idServicio) {
        return servicioDAO.obtenerServicioPorId(idServicio);
    }

    /**
     * Obtiene una lista de todos los servicios (activos e inactivos).
     * @return Lista de objetos Servicio.
     */
    public List<Servicio> obtenerTodosLosServicios() {
        return servicioDAO.obtenerTodosLosServicios();
    }

    /**
     * Obtiene una lista de servicios activos (disponibles).
     * @return Lista de objetos Servicio activos.
     */
    public List<Servicio> obtenerServiciosActivos() {
        return servicioDAO.obtenerTodosLosServicios().stream()
                .filter(Servicio::isActivo)
                .toList();
    }

    /**
     * Obtiene una lista de servicios por el nombre de su categoría.
     * @param nombreCategoria El nombre de la categoría de servicio.
     * @return Lista de servicios de esa categoría.
     */
    public List<Servicio> obtenerServiciosPorNombreCategoria(String nombreCategoria) {
        CategoriaServicio categoria = categoriaServicioDAO.obtenerCategoriaServicioPorNombre(nombreCategoria);
        if (categoria != null) {
            return servicioDAO.obtenerServiciosPorCategoria(categoria.getIdCategoriaServicio());
        }
        return List.of(); // Retorna lista vacía si la categoría no existe
    }
}