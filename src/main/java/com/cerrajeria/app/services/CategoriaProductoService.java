package com.cerrajeria.app.services;

import com.cerrajeria.app.dao.CategoriaProductoDAO;
import com.cerrajeria.app.models.CategoriaProducto;

import java.util.List;

/**
 * Clase de servicio para la gestión de categorías de productos.
 * Contiene la lógica de negocio para crear, editar y desactivar categorías.
 */
public class CategoriaProductoService {

    private CategoriaProductoDAO categoriaProductoDAO;

    // Constructor
    public CategoriaProductoService() {
        this.categoriaProductoDAO = new CategoriaProductoDAO();
    }

    /**
     * Crea una nueva categoría de producto.
     * @param nombre Nombre de la categoría.
     * @return true si la categoría fue creada exitosamente, false en caso contrario.
     */
    public boolean crearCategoriaProducto(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.err.println("Error al crear categoría de producto: El nombre no puede estar vacío.");
            return false;
        }
        if (categoriaProductoDAO.obtenerCategoriaProductoPorNombre(nombre) != null) {
            System.err.println("Error al crear categoría de producto: Una categoría con este nombre ya existe.");
            return false;
        }

        CategoriaProducto nuevaCategoria = new CategoriaProducto(nombre);
        int id = categoriaProductoDAO.insertarCategoriaProducto(nuevaCategoria);
        return id != -1;
    }

    /**
     * Actualiza los datos de una categoría de producto existente.
     * @param categoria El objeto CategoriaProducto con los datos actualizados.
     * @return true si la actualización es exitosa, false en caso contrario.
     */
    public boolean actualizarCategoriaProducto(CategoriaProducto categoria) {
        if (categoria == null || categoria.getIdCategoriaProducto() <= 0) {
            System.err.println("Error al actualizar categoría de producto: ID de categoría inválido.");
            return false;
        }
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            System.err.println("Error al actualizar categoría de producto: El nombre no puede estar vacío.");
            return false;
        }
        // Opcional: Verificar si el nuevo nombre ya existe en otra categoría (excluyendo la actual)
        CategoriaProducto categoriaExistenteConMismoNombre = categoriaProductoDAO.obtenerCategoriaProductoPorNombre(categoria.getNombre());
        if (categoriaExistenteConMismoNombre != null && categoriaExistenteConMismoNombre.getIdCategoriaProducto() != categoria.getIdCategoriaProducto()) {
            System.err.println("Error al actualizar categoría de producto: Ya existe otra categoría con el nombre '" + categoria.getNombre() + "'.");
            return false;
        }

        return categoriaProductoDAO.actualizarCategoriaProducto(categoria);
    }

    /**
     * Desactiva lógicamente una categoría de producto.
     * @param idCategoria ID de la categoría a desactivar.
     * @return true si la desactivación es exitosa, false en caso contrario.
     */
    public boolean desactivarCategoriaProducto(int idCategoria) {
        // Evitar desactivar la categoría "Otros" (siempre debe estar activa para productos "Otros")
        CategoriaProducto categoria = categoriaProductoDAO.obtenerCategoriaProductoPorId(idCategoria);
        if (categoria != null && "Otros".equalsIgnoreCase(categoria.getNombre())) {
            System.err.println("No se puede desactivar la categoría 'Otros'.");
            return false;
        }
        return categoriaProductoDAO.desactivarCategoriaProducto(idCategoria);
    }

    /**
     * Activa lógicamente una categoría de producto.
     * @param idCategoria ID de la categoría a activar.
     * @return true si la activación es exitosa, false en caso contrario.
     */
    public boolean activarCategoriaProducto(int idCategoria) {
        return categoriaProductoDAO.activarCategoriaProducto(idCategoria);
    }

    /**
     * Obtiene una categoría de producto por su ID.
     * @param idCategoria ID de la categoría.
     * @return El objeto CategoriaProducto o null.
     */
    public CategoriaProducto obtenerCategoriaProductoPorId(int idCategoria) {
        return categoriaProductoDAO.obtenerCategoriaProductoPorId(idCategoria);
    }

    /**
     * Obtiene una categoría de producto por su nombre.
     * @param nombre Nombre de la categoría.
     * @return El objeto CategoriaProducto o null.
     */
    public CategoriaProducto obtenerCategoriaProductoPorNombre(String nombre) {
        return categoriaProductoDAO.obtenerCategoriaProductoPorNombre(nombre);
    }

    /**
     * Obtiene una lista de todas las categorías de producto (activas e inactivas).
     * @return Lista de objetos CategoriaProducto.
     */
    public List<CategoriaProducto> obtenerTodasCategoriasProducto() {
        return categoriaProductoDAO.obtenerTodasCategoriasProducto();
    }

    /**
     * Obtiene una lista de categorías de producto activas.
     * @return Lista de objetos CategoriaProducto activos.
     */
    public List<CategoriaProducto> obtenerCategoriasProductoActivas() {
        return categoriaProductoDAO.obtenerTodasCategoriasProducto().stream()
                .filter(CategoriaProducto::isActivo)
                .toList();
    }
}