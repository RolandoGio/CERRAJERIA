package com.cerrajeria.app.services;

import com.cerrajeria.app.dao.*;
import com.cerrajeria.app.models.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase de servicio para la generación de reportes y visualización global.
 * Accede a diversos DAOs para compilar información agregada y estadísticas.
 */
public class ReporteService {

    private VentaDAO ventaDAO;
    private ComisionDAO comisionDAO;
    private UsuarioDAO usuarioDAO;
    private ProductoDAO productoDAO;
    private ServicioDAO servicioDAO;
    private DetalleVentaProductoDAO detalleVentaProductoDAO;
    private DetalleVentaServicioDAO detalleVentaServicioDAO;
    private ControlFinancieroDAO controlFinancieroDAO;

    // Constructor
    public ReporteService() {
        this.ventaDAO = new VentaDAO();
        this.comisionDAO = new ComisionDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.productoDAO = new ProductoDAO();
        this.servicioDAO = new ServicioDAO();
        this.detalleVentaProductoDAO = new DetalleVentaProductoDAO();
        this.detalleVentaServicioDAO = new DetalleVentaServicioDAO();
        this.controlFinancieroDAO = new ControlFinancieroDAO();
    }

    /**
     * Obtiene todas las ventas, opcionalmente filtradas por fecha y/o por usuario.
     * Utiliza la fecha de creación de la venta para el filtro temporal.
     * @param desde Fecha y hora de inicio (puede ser null).
     * @param hasta Fecha y hora de fin (puede ser null).
     * @param idUsuario ID del usuario (vendedor) para filtrar (puede ser null para todas las ventas).
     * @return Lista de ventas que cumplen con los criterios.
     */
    public List<Venta> obtenerTodasLasVentasFiltradas(LocalDateTime desde, LocalDateTime hasta, Integer idUsuario) {
        List<Venta> ventas = ventaDAO.obtenerTodasLasVentas(); // Obtener todas y filtrar en memoria

        return ventas.stream()
                .filter(venta -> (desde == null || (venta.getFechaCreacion() != null && !venta.getFechaCreacion().isBefore(desde)))) // Usar getFechaCreacion
                .filter(venta -> (hasta == null || (venta.getFechaCreacion() != null && !venta.getFechaCreacion().isAfter(hasta))))   // Usar getFechaCreacion
                .filter(venta -> (idUsuario == null || venta.getIdUsuario() == idUsuario))
                .sorted(Comparator.comparing(Venta::getFechaCreacion).reversed()) // Ordenar por fecha de creación, más recientes primero
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las comisiones registradas, opcionalmente filtradas por usuario o estado.
     * @param idUsuario ID del usuario (puede ser null para todas las comisiones).
     * @param estado Estado de la comisión (ej. "Pendiente", "Aprobado", "Rechazado", "Pagado") (puede ser null para todos los estados).
     * @return Lista de comisiones que cumplen con los criterios.
     */
    public List<Comision> obtenerTodasLasComisionesFiltradas(Integer idUsuario, String estado) {
        List<Comision> comisiones = comisionDAO.obtenerTodasLasComisiones();

        return comisiones.stream()
                .filter(comision -> (idUsuario == null || comision.getIdUsuario() == idUsuario))
                .filter(comision -> (estado == null || comision.getEstado().equalsIgnoreCase(estado)))
                .sorted(Comparator.comparing(Comision::getFechaCreacion).reversed()) // Ordenar por fecha de creación
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el historial detallado de un vendedor (ventas, comisiones).
     * @param idVendedor ID del vendedor.
     * @return Un mapa que contiene la lista de ventas y la lista de comisiones del vendedor.
     */
    public Map<String, List<?>> obtenerHistorialVendedor(int idVendedor) {
        Map<String, List<?>> historial = new LinkedHashMap<>(); // LinkedHashMap para mantener el orden de inserción

        List<Venta> ventasVendedor = ventaDAO.obtenerVentasPorUsuario(idVendedor);
        List<Comision> comisionesVendedor = comisionDAO.obtenerComisionesPorUsuario(idVendedor);

        // Cargar detalles para cada venta del vendedor
        // No es estrictamente necesario cargar los detalles aquí si solo se listan ventas/comisiones,
        // pero se mantiene la estructura por si la UI los necesita.
        for (Venta venta : ventasVendedor) {
            List<DetalleVentaProducto> productos = detalleVentaProductoDAO.obtenerDetallesPorVenta(venta.getIdVenta());
            List<DetalleVentaServicio> servicios = detalleVentaServicioDAO.obtenerDetallesPorVenta(venta.getIdVenta());
            // Si el modelo Venta tuviera listas para detalles, se podrían asignar aquí:
            // venta.setDetallesProductos(productos);
            // venta.setDetallesServicios(servicios);
        }

        historial.put("ventas", ventasVendedor);
        historial.put("comisiones", comisionesVendedor);

        return historial;
    }

    /**
     * Obtiene las estadísticas de productos más vendidos.
     * @param limite Número máximo de productos a retornar.
     * @return Mapa de nombre de producto a cantidad total vendida.
     */
    public Map<String, Integer> obtenerProductosMasVendidos(int limite) {
        List<DetalleVentaProducto> todosLosDetalles = detalleVentaProductoDAO.obtenerTodosLosDetallesVentaProducto();

        return todosLosDetalles.stream()
                .collect(Collectors.groupingBy(DetalleVentaProducto::getIdProducto,
                        Collectors.summingInt(DetalleVentaProducto::getCantidad)))
                .entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()) // Ordenar por cantidad descendente
                .limit(limite)
                .collect(Collectors.toMap(
                        entry -> { // Mapear ID de producto a su nombre
                            Producto producto = new ProductoDAO().obtenerProductoPorId(entry.getKey());
                            return (producto != null) ? producto.getNombre() : "Producto Desconocido (ID: " + entry.getKey() + ")";
                        },
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // Merge function for duplicates (shouldn't happen with groupingBy)
                        LinkedHashMap::new // Para mantener el orden de inserción (después del sort)
                ));
    }

    /**
     * Obtiene las estadísticas de servicios más vendidos.
     * @param limite Número máximo de servicios a retornar.
     * @return Mapa de nombre de servicio a cantidad total vendida.
     */
    public Map<String, Integer> obtenerServiciosMasVendidos(int limite) {
        List<DetalleVentaServicio> todosLosDetalles = detalleVentaServicioDAO.obtenerTodosLosDetallesVentaServicio();

        return todosLosDetalles.stream()
                .collect(Collectors.groupingBy(DetalleVentaServicio::getIdServicio,
                        Collectors.summingInt(DetalleVentaServicio::getCantidad)))
                .entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()) // Ordenar por cantidad descendente
                .limit(limite)
                .collect(Collectors.toMap(
                        entry -> { // Mapear ID de servicio a su nombre
                            Servicio servicio = new ServicioDAO().obtenerServicioPorId(entry.getKey());
                            return (servicio != null) ? servicio.getNombre() : "Servicio Desconocido (ID: " + entry.getKey() + ")";
                        },
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Obtiene un resumen financiero (total ingresos, egresos, balance).
     * @return Un mapa con los totales financieros.
     */
    public Map<String, BigDecimal> obtenerResumenFinanciero() {
        BigDecimal totalIngresos = controlFinancieroDAO.obtenerControlFinancieroPorTipo("Ingreso").stream()
                .map(ControlFinanciero::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEgresos = controlFinancieroDAO.obtenerControlFinancieroPorTipo("Egreso").stream()
                .map(ControlFinanciero::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> resumen = new LinkedHashMap<>();
        resumen.put("totalIngresos", totalIngresos);
        resumen.put("totalEgresos", totalEgresos);
        resumen.put("balanceTotal", totalIngresos.subtract(totalEgresos));
        return resumen;
    }

    /**
     * Obtiene el listado de todos los movimientos de stock para visualizar.
     * @return Lista de MovimientoStock.
     */
    public List<MovimientoStock> obtenerTodosLosMovimientosDeStock() {
        return new MovimientoStockDAO().obtenerTodosLosMovimientosStock();
    }

    /**
     * Obtiene todos los usuarios (vendedores y administradores) para la gestión.
     * @return Lista de Usuario.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioDAO.obtenerTodosLosUsuarios();
    }

    /**
     * Obtiene todas las categorías de producto para la gestión.
     * @return Lista de CategoriaProducto.
     */
    public List<CategoriaProducto> obtenerTodasLasCategoriasProducto() {
        return new CategoriaProductoDAO().obtenerTodasCategoriasProducto();
    }

    /**
     * Obtiene todas las categorías de servicio para la gestión.
     * @return Lista de CategoriaServicio.
     */
    public List<CategoriaServicio> obtenerTodasLasCategoriasServicio() {
        return new CategoriaServicioDAO().obtenerTodasCategoriasServicio();
    }
}