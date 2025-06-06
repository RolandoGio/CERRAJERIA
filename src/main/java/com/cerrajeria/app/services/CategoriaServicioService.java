package com.cerrajeria.app.services;

import com.cerrajeria.app.dao.CategoriaServicioDAO;
import com.cerrajeria.app.models.CategoriaServicio;

import java.util.List;

/**
 * Clase de servicio para la gestión de categorías de servicios.
 * Contiene la lógica de negocio para crear, editar y desactivar categorías de servicio.
 */
public class CategoriaServicioService {

    private CategoriaServicioDAO categoriaServicioDAO;

    // Constructor
    public CategoriaServicioService() {
        this.categoriaServicioDAO = new CategoriaServicioDAO();
    }

    /**
     * Crea una nueva categoría de servicio.
     * Realiza validaciones para asegurar que el nombre no esté vacío y no sea duplicado.
     * @param nombre Nombre de la categoría de servicio.
     * @return true si la categoría fue creada exitosamente, false en caso contrario.
     */
    public boolean crearCategoriaServicio(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.err.println("Error al crear categoría de servicio: El nombre no puede estar vacío.");
            return false;
        }
        if (categoriaServicioDAO.obtenerCategoriaServicioPorNombre(nombre) != null) {
            System.err.println("Error al crear categoría de servicio: Una categoría con este nombre ya existe.");
            return false;
        }

        CategoriaServicio nuevaCategoria = new CategoriaServicio(nombre);
        int id = categoriaServicioDAO.insertarCategoriaServicio(nuevaCategoria);
        return id != -1;
    }

    /**
     * Actualiza los datos de una categoría de servicio existente.
     * Realiza validaciones para asegurar que el ID sea válido y el nombre no sea duplicado por otra categoría.
     * @param categoria El objeto CategoriaServicio con los datos actualizados.
     * @return true si la actualización es exitosa, false en caso contrario.
     */
    public boolean actualizarCategoriaServicio(CategoriaServicio categoria) {
        if (categoria == null || categoria.getIdCategoriaServicio() <= 0) {
            System.err.println("Error al actualizar categoría de servicio: ID de categoría inválido.");
            return false;
        }
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            System.err.println("Error al actualizar categoría de servicio: El nombre no puede estar vacío.");
            return false;
        }
        // Verificar si el nuevo nombre ya existe en otra categoría (excluyendo la actual)
        CategoriaServicio categoriaExistenteConMismoNombre = categoriaServicioDAO.obtenerCategoriaServicioPorNombre(categoria.getNombre());
        if (categoriaExistenteConMismoNombre != null && categoriaExistenteConMismoNombre.getIdCategoriaServicio() != categoria.getIdCategoriaServicio()) {
            System.err.println("Error al actualizar categoría de servicio: Ya existe otra categoría con el nombre '" + categoria.getNombre() + "'.");
            return false;
        }

        return categoriaServicioDAO.actualizarCategoriaServicio(categoria);
    }

    /**
     * Desactiva lógicamente una categoría de servicio.
     * Se incluye una validación para evitar desactivar la categoría "Otros" (si existe).
     * @param idCategoria ID de la categoría a desactivar.
     * @return true si la desactivación es exitosa, false en caso contrario.
     */
    public boolean desactivarCategoriaServicio(int idCategoria) {
        // Evitar desactivar la categoría "Otros" (siempre debe estar activa para servicios "Otros")
        CategoriaServicio categoria = categoriaServicioDAO.obtenerCategoriaServicioPorId(idCategoria);
        if (categoria != null && "Otros".equalsIgnoreCase(categoria.getNombre())) {
            System.err.println("No se puede desactivar la categoría de servicio 'Otros'.");
            return false;
        }
        return categoriaServicioDAO.desactivarCategoriaServicio(idCategoria);
    }

    /**
     * Activa lógicamente una categoría de servicio.
     * @param idCategoria ID de la categoría a activar.
     * @return true si la activación es exitosa, false en caso contrario.
     */
    public boolean activarCategoriaServicio(int idCategoria) {
        return categoriaServicioDAO.activarCategoriaServicio(idCategoria);
    }

    /**
     * Obtiene una categoría de servicio por su ID.
     * @param idCategoria ID de la categoría.
     * @return El objeto CategoriaServicio o null.
     */
    public CategoriaServicio obtenerCategoriaServicioPorId(int idCategoria) {
        return categoriaServicioDAO.obtenerCategoriaServicioPorId(idCategoria);
    }

    /**
     * Obtiene una categoría de servicio por su nombre.
     * @param nombre Nombre de la categoría.
     * @return El objeto CategoriaServicio o null.
     */
    public CategoriaServicio obtenerCategoriaServicioPorNombre(String nombre) {
        return categoriaServicioDAO.obtenerCategoriaServicioPorNombre(nombre);
    }

    /**
     * Obtiene una lista de todas las categorías de servicio (activas e inactivas).
     * @return Lista de objetos CategoriaServicio.
     */
    public List<CategoriaServicio> obtenerTodasCategoriasServicio() {
        return categoriaServicioDAO.obtenerTodasCategoriasServicio();
    }

    /**
     * Obtiene una lista de categorías de servicio activas.
     * @return Lista de objetos CategoriaServicio activos.
     */
    public List<CategoriaServicio> obtenerCategoriasServicioActivas() {
        return categoriaServicioDAO.obtenerTodasCategoriasServicio().stream()
                .filter(CategoriaServicio::isActivo)
                .toList();
    }
}