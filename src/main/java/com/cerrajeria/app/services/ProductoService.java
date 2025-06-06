package com.cerrajeria.app.services;

import com.cerrajeria.app.dao.ProductoDAO;
import com.cerrajeria.app.dao.CategoriaProductoDAO;
import com.cerrajeria.app.models.Producto;
import com.cerrajeria.app.models.CategoriaProducto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Clase de servicio para la gestión de productos.
 * Contiene la lógica de negocio para crear, editar, visualizar y desactivar productos.
 */
public class ProductoService {

    private ProductoDAO productoDAO;
    private CategoriaProductoDAO categoriaProductoDAO; // Para verificar la existencia de categorías

    // Constructor
    public ProductoService() {
        this.productoDAO = new ProductoDAO();
        this.categoriaProductoDAO = new CategoriaProductoDAO();
    }

    /**
     * Crea un nuevo producto con validaciones y asignación de estado inicial.
     * @param nombre Nombre del producto.
     * @param idCategoriaProducto ID de la categoría a la que pertenece.
     * @param costoInterno Costo de adquisición del producto.
     * @param precio Precio de venta al público.
     * @param stockInicial Cantidad inicial en stock.
     * @param stockMinimo Umbral para considerar el stock bajo.
     * @return true si el producto fue creado exitosamente, false en caso contrario.
     */
    public boolean crearProducto(String nombre, int idCategoriaProducto, BigDecimal costoInterno,
                                 BigDecimal precio, int stockInicial, int stockMinimo) {
        // Validaciones básicas
        if (nombre == null || nombre.trim().isEmpty() || precio == null || precio.compareTo(BigDecimal.ZERO) < 0 ||
                stockInicial < 0 || stockMinimo < 0 || costoInterno == null || costoInterno.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Error al crear producto: Campos obligatorios o valores inválidos.");
            return false;
        }

        // Verificar si la categoría existe y está activa
        CategoriaProducto categoria = categoriaProductoDAO.obtenerCategoriaProductoPorId(idCategoriaProducto);
        if (categoria == null || !categoria.isActivo()) {
            System.err.println("Error al crear producto: La categoría de producto especificada no existe o está inactiva.");
            return false;
        }

        // Determinar el estado inicial del stock
        String estadoInicial;
        if (stockInicial <= 0) {
            estadoInicial = "Agotado";
        } else if (stockInicial <= stockMinimo) {
            estadoInicial = "Bajo";
        } else {
            estadoInicial = "Disponible";
        }

        Producto nuevoProducto = new Producto(nombre, idCategoriaProducto, precio, stockInicial,
                stockMinimo, costoInterno);
        nuevoProducto.setEstado(estadoInicial); // Establecer el estado inicial

        int id = productoDAO.insertarProducto(nuevoProducto);
        return id != -1;
    }

    /**
     * Actualiza los datos de un producto existente.
     * Se recalcula el estado del stock si la cantidad cambia.
     * @param producto El objeto Producto con los datos actualizados.
     * @return true si la actualización es exitosa, false en caso contrario.
     */
    public boolean actualizarProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() <= 0) {
            System.err.println("Error al actualizar producto: ID de producto inválido.");
            return false;
        }
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty() ||
                producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) < 0 ||
                producto.getStock() < 0 || producto.getStockMinimo() < 0 ||
                producto.getCostoInterno() == null || producto.getCostoInterno().compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Error al actualizar producto: Campos obligatorios o valores inválidos.");
            return false;
        }

        // Verificar si la categoría existe y está activa (si se cambió)
        CategoriaProducto categoria = categoriaProductoDAO.obtenerCategoriaProductoPorId(producto.getIdCategoriaProducto());
        if (categoria == null || !categoria.isActivo()) {
            System.err.println("Error al actualizar producto: La categoría de producto especificada no existe o está inactiva.");
            return false;
        }

        // Recalcular el estado del stock si el stock o stockMinimo han cambiado
        String nuevoEstado;
        if (producto.getStock() <= 0) {
            nuevoEstado = "Agotado";
        } else if (producto.getStock() <= producto.getStockMinimo()) {
            nuevoEstado = "Bajo";
        } else {
            nuevoEstado = "Disponible";
        }
        producto.setEstado(nuevoEstado);

        return productoDAO.actualizarProducto(producto);
    }

    /**
     * Desactiva lógicamente un producto.
     * @param idProducto ID del producto a desactivar.
     * @return true si la desactivación es exitosa, false en caso contrario.
     */
    public boolean desactivarProducto(int idProducto) {
        return productoDAO.desactivarProducto(idProducto);
    }

    /**
     * Activa lógicamente un producto.
     * @param idProducto ID del producto a activar.
     * @return true si la activación es exitosa, false en caso contrario.
     */
    public boolean activarProducto(int idProducto) {
        return productoDAO.activarProducto(idProducto);
    }

    /**
     * Obtiene un producto por su ID.
     * @param idProducto ID del producto.
     * @return El objeto Producto o null.
     */
    public Producto obtenerProductoPorId(int idProducto) {
        return productoDAO.obtenerProductoPorId(idProducto);
    }

    /**
     * Obtiene una lista de todos los productos (activos e inactivos).
     * @return Lista de objetos Producto.
     */
    public List<Producto> obtenerTodosLosProductos() {
        return productoDAO.obtenerTodosLosProductos();
    }

    /**
     * Obtiene una lista de productos activos (disponibles).
     * @return Lista de objetos Producto.
     */
    public List<Producto> obtenerProductosActivos() {
        // Filtrar en memoria por simplicidad, si la lista no es enorme.
        // Para grandes datasets, se preferiría un método en el DAO que filtre directamente en DB.
        return productoDAO.obtenerTodosLosProductos().stream()
                .filter(Producto::isActivo)
                .toList();
    }

    /**
     * Obtiene una lista de productos por su estado de stock (Disponible, Bajo, Agotado).
     * @param estado El estado del stock a filtrar.
     * @return Lista de productos que coinciden con el estado.
     */
    public List<Producto> obtenerProductosPorEstadoStock(String estado) {
        // Se puede filtrar en el DAO si se requiere mayor eficiencia, o aquí en el servicio
        return productoDAO.obtenerTodosLosProductos().stream()
                .filter(p -> p.getEstado().equalsIgnoreCase(estado))
                .toList();
    }

    /**
     * Obtiene una lista de productos por el nombre de su categoría.
     * @param nombreCategoria El nombre de la categoría.
     * @return Lista de productos de esa categoría.
     */
    public List<Producto> obtenerProductosPorNombreCategoria(String nombreCategoria) {
        CategoriaProducto categoria = categoriaProductoDAO.obtenerCategoriaProductoPorNombre(nombreCategoria);
        if (categoria != null) {
            return productoDAO.obtenerTodosLosProductos().stream()
                    .filter(p -> p.getIdCategoriaProducto() == categoria.getIdCategoriaProducto())
                    .toList();
        }
        return List.of(); // Retorna lista vacía si la categoría no existe
    }
}