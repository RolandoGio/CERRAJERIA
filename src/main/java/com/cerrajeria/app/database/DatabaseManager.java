package com.cerrajeria.app.database;

import com.cerrajeria.app.dao.UsuarioDAO;
import com.cerrajeria.app.models.Usuario;
import com.cerrajeria.app.dao.CategoriaProductoDAO;
import com.cerrajeria.app.models.CategoriaProducto;
import com.cerrajeria.app.dao.ProductoDAO;
import com.cerrajeria.app.models.Producto;
import com.cerrajeria.app.dao.MovimientoStockDAO;
import com.cerrajeria.app.models.MovimientoStock;
import com.cerrajeria.app.dao.ServicioDAO;
import com.cerrajeria.app.models.Servicio;
import com.cerrajeria.app.dao.VentaDAO;
import com.cerrajeria.app.models.Venta;
import com.cerrajeria.app.dao.DetalleVentaProductoDAO;
import com.cerrajeria.app.models.DetalleVentaProducto;
import com.cerrajeria.app.dao.DetalleVentaServicioDAO;
import com.cerrajeria.app.models.DetalleVentaServicio;
import com.cerrajeria.app.dao.ComisionCategoriaProductoDAO;
import com.cerrajeria.app.models.ComisionCategoriaProducto;
import com.cerrajeria.app.dao.ComisionDAO;
import com.cerrajeria.app.models.Comision;
import com.cerrajeria.app.dao.ControlFinancieroDAO;
import com.cerrajeria.app.models.ControlFinanciero;
import com.cerrajeria.app.dao.CategoriaServicioDAO;
import com.cerrajeria.app.models.CategoriaServicio;

// Servicios
import com.cerrajeria.app.services.UsuarioService;
import com.cerrajeria.app.services.ProductoService;
import com.cerrajeria.app.services.CategoriaProductoService;
import com.cerrajeria.app.services.CategoriaServicioService;
import com.cerrajeria.app.services.ServicioService;
import com.cerrajeria.app.services.VentaService;
import com.cerrajeria.app.services.ComisionService;
import com.cerrajeria.app.services.ControlFinancieroService;
import com.cerrajeria.app.services.MovimientoStockService;
import com.cerrajeria.app.services.ReporteService; // NUEVO

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map; // Para los mapas de los reportes

/**
 * Clase para gestionar la conexión a la base de datos Microsoft SQL Server
 * y demostrar el uso de los DAOs y Servicios en modo de SOLO LECTURA.
 * Todas las operaciones de escritura en los servicios están comentadas.
 */
public class DatabaseManager {

    private static final String SERVER_NAME = "localhost";
    private static final String DATABASE_NAME = "cerrajeria"; // Nombre de tu base de datos

    private static final String CONNECTION_URL = "jdbc:sqlserver://" + SERVER_NAME + ";"
            + "databaseName=" + DATABASE_NAME + ";"
            + "integratedSecurity=true;"
            + "trustServerCertificate=true;";

    /**
     * Establece y retorna una conexión a la base de datos SQL Server.
     * @return Una instancia de Connection si la conexión fue exitosa, o null si hubo un error.
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(CONNECTION_URL);
            System.out.println("Conexión a SQL Server establecida exitosamente.");
        } catch (SQLException e) {
            System.err.println("Error al conectar a SQL Server: " + e.getMessage());
            System.err.println("Asegúrate de que:");
            System.err.println("  1. Tu instancia de SQL Server esté ejecutándose.");
            System.err.println("  2. La base de datos '" + DATABASE_NAME + "' exista en tu SQL Server.");
            System.err.println("  3. Tu usuario de Windows tenga permisos para acceder a esa base de datos.");
        }
        return conn;
    }

    /**
     * Método principal para probar la conexión a la base de datos
     * y las funcionalidades de los DAOs y Servicios en modo de SOLO LECTURA.
     */
    public static void main(String[] args) {
        System.out.println("Iniciando aplicación de prueba (solo lectura)...");
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("¡Conexión exitosa!");

                // --- Demostración de DAOs (Lectura) ---
                System.out.println("\n--- PRUEBAS DE USUARIOS (Lectura) ---");
                new UsuarioDAO().obtenerTodosLosUsuarios().forEach(System.out::println);
                System.out.println("\n--- PRUEBAS DE CATEGORÍAS DE PRODUCTO (Lectura) ---");
                new CategoriaProductoDAO().obtenerTodasCategoriasProducto().forEach(System.out::println);
                System.out.println("\n--- PRUEBAS DE PRODUCTOS (Lectura) ---");
                new ProductoDAO().obtenerTodosLosProductos().forEach(System.out::println);
                System.out.println("\n--- PRUEBAS DE MOVIMIENTO DE STOCK (Lectura) ---");
                new MovimientoStockDAO().obtenerTodosLosMovimientosStock().forEach(System.out::println);
                System.out.println("\n--- PRUEBAS DE SERVICIOS (Lectura) ---");
                new ServicioDAO().obtenerTodosLosServicios().forEach(System.out::println);
                System.out.println("\n--- PRUEBAS DE VENTAS (Lectura) ---");
                new VentaDAO().obtenerTodasLasVentas().forEach(System.out::println);
                System.out.println("\n--- PRUEBAS DE DETALLES DE VENTA DE PRODUCTO (Lectura) ---");
                new DetalleVentaProductoDAO().obtenerTodosLosDetallesVentaProducto().forEach(System.out::println);
                System.out.println("\n--- PRUEBAS DE DETALLES DE VENTA DE SERVICIO (Lectura) ---");
                new DetalleVentaServicioDAO().obtenerTodosLosDetallesVentaServicio().forEach(System.out::println);
                System.out.println("\n--- PRUEBAS DE COMISIÓN POR CATEGORÍA DE PRODUCTO (Lectura) ---");
                new ComisionCategoriaProductoDAO().obtenerTodasLasComisionesCategoriaProducto().forEach(System.out::println);
                System.out.println("\n--- PRUEBAS DE COMISIONES INDIVIDUALES (Lectura) ---");
                new ComisionDAO().obtenerTodasLasComisiones().forEach(System.out::println);
                System.out.println("\n--- PRUEBAS DE CONTROL FINANCIERO (Lectura) ---");
                new ControlFinancieroDAO().obtenerTodosLosRegistrosFinancieros().forEach(System.out::println);
                System.out.println("\n--- PRUEBAS DE CATEGORÍAS DE SERVICIO (Lectura) ---");
                new CategoriaServicioDAO().obtenerTodasCategoriasServicio().forEach(System.out::println);


                // =========================================================================
                // --- PRUEBAS DEL USUARIOSERVICE (SOLO LECTURA) ---
                // =========================================================================
                System.out.println("\n\n--- INICIANDO PRUEBAS DE USUARIOSERVICE (SOLO LECTURA) ---");
                UsuarioService usuarioService = new UsuarioService();
                System.out.println("\n--- Prueba de autenticación por nombre de usuario 'admin' (Contraseña: 2025) ---");
                Usuario authAdmin = usuarioService.autenticarUsuario("admin", "2025");
                if (authAdmin != null) { System.out.println("Autenticación exitosa: " + authAdmin.getNombreUsuario()); } else { System.out.println("Autenticación fallida para 'admin'."); }
                System.out.println("\n--- Prueba de autenticación por CÓDIGO 'AD0001' (Admin) (Contraseña: 2025) ---");
                Usuario authAdminByCode = usuarioService.autenticarUsuarioPorCodigo("AD0001", "2025");
                if (authAdminByCode != null) { System.out.println("Autenticación exitosa por código: " + authAdminByCode.getNombre() + " (" + authAdminByCode.getCodigo() + ") Rol: " + authAdminByCode.getRol()); } else { System.out.println("Autenticación fallida para 'AD0001'."); }
                System.out.println("\n--- Prueba de autenticación por CÓDIGO 'VD0001' (Vendedor) (Contraseña: 2025) ---");
                Usuario authVendedorByCode = usuarioService.autenticarUsuarioPorCodigo("VD0001", "2025");
                if (authVendedorByCode != null) { System.out.println("Autenticación exitosa por código: " + authVendedorByCode.getNombre() + " (" + authVendedorByCode.getCodigo() + ") Rol: " + authVendedorByCode.getRol()); } else { System.out.println("Autenticación fallida para 'VD0001'."); }
                System.out.println("\n--- Prueba de autenticación por CÓDIGO fallida (contraseña incorrecta) ---");
                if (usuarioService.autenticarUsuarioPorCodigo("AD0001", "wrongpass") == null) { System.out.println("Autenticación fallida por código (esperado)."); }
                System.out.println("\n--- Prueba de autenticación por CÓDIGO fallida (código inexistente) ---");
                if (usuarioService.autenticarUsuarioPorCodigo("XX9999", "anypass") == null) { System.out.println("Autenticación fallida por código (esperado)."); }
                System.out.println("\n--- Fin de pruebas de UsuarioService ---");


                // =========================================================================
                // --- PRUEBAS DE CATEGORIA PRODUCTO SERVICE (SOLO LECTURA) ---
                // =========================================================================
                System.out.println("\n\n--- INICIANDO PRUEBAS DE CATEGORIA PRODUCTO SERVICE (SOLO LECTURA) ---");
                CategoriaProductoService categoriaProductoService = new CategoriaProductoService();
                System.out.println("\n--- Listando todas las categorías de producto ---");
                categoriaProductoService.obtenerTodasCategoriasProducto().forEach(System.out::println);
                System.out.println("\n--- Listando categorías de producto ACTIVAS ---");
                categoriaProductoService.obtenerCategoriasProductoActivas().forEach(System.out::println);
                System.out.println("\n--- Fin de pruebas de CategoriaProductoService ---");


                // =========================================================================
                // --- PRUEBAS DE PRODUCTO SERVICE (SOLO LECTURA) ---
                // =========================================================================
                System.out.println("\n\n--- INICIANDO PRUEBAS DE PRODUCTO SERVICE (SOLO LECTURA) ---");
                ProductoService productoService = new ProductoService();
                System.out.println("\n--- Listando todos los productos ---");
                productoService.obtenerTodosLosProductos().forEach(System.out::println);
                System.out.println("\n--- Listando productos ACTIVOS ---");
                productoService.obtenerProductosActivos().forEach(System.out::println);
                System.out.println("\n--- Listando productos con estado 'Bajo' ---");
                productoService.obtenerProductosPorEstadoStock("Bajo").forEach(System.out::println);
                System.out.println("\n--- Listando productos de categoría 'Otros' ---");
                productoService.obtenerProductosPorNombreCategoria("Otros").forEach(System.out::println);
                System.out.println("\n--- Fin de pruebas de ProductoService ---");


                // =========================================================================
                // --- PRUEBAS DE CATEGORIA SERVICIO SERVICE (SOLO LECTURA) ---
                // =========================================================================
                System.out.println("\n\n--- INICIANDO PRUEBAS DE CATEGORIA SERVICIO SERVICE (SOLO LECTURA) ---");
                CategoriaServicioService categoriaServicioService = new CategoriaServicioService();
                System.out.println("\n--- Listando todas las categorías de servicio ---");
                categoriaServicioService.obtenerTodasCategoriasServicio().forEach(System.out::println);
                System.out.println("\n--- Listando categorías de servicio ACTIVAS ---");
                categoriaServicioService.obtenerCategoriasServicioActivas().forEach(System.out::println);
                System.out.println("\n--- Fin de pruebas de CategoriaServicioService ---");


                // =========================================================================
                // --- PRUEBAS DE SERVICIO SERVICE (SOLO LECTURA) ---
                // =========================================================================
                System.out.println("\n\n--- INICIANDO PRUEBAS DE SERVICIO SERVICE (SOLO LECTURA) ---");
                ServicioService servicioService = new ServicioService();
                System.out.println("\n--- Listando todos los servicios ---");
                servicioService.obtenerTodosLosServicios().forEach(System.out::println);
                System.out.println("\n--- Listando servicios ACTIVOS ---");
                servicioService.obtenerServiciosActivos().forEach(System.out::println);
                System.out.println("\n--- Listando servicios de categoría 'Otros' ---");
                servicioService.obtenerServiciosPorNombreCategoria("Otros").forEach(System.out::println);
                System.out.println("\n--- Fin de pruebas de ServicioService ---");


                // =========================================================================
                // --- PRUEBAS DE VENTA SERVICE (SOLO LECTURA) ---
                // =========================================================================
                System.out.println("\n\n--- INICIANDO PRUEBAS DE VENTA SERVICE (SOLO LECTURA) ---");
                VentaService ventaService = new VentaService();

                System.out.println("\n--- Listando TODAS las ventas ---");
                ventaService.obtenerTodasLasVentas().forEach(System.out::println);

                List<Venta> todasLasVentas = ventaService.obtenerTodasLasVentas();
                if (!todasLasVentas.isEmpty()) {
                    Venta primeraVenta = todasLasVentas.get(0);
                    System.out.println("\n--- Detalles de productos para la primera venta (ID: " + primeraVenta.getIdVenta() + ") ---");
                    ventaService.obtenerDetallesProductoPorVenta(primeraVenta.getIdVenta()).forEach(System.out::println);
                    System.out.println("\n--- Detalles de servicios para la primera venta (ID: " + primeraVenta.getIdVenta() + ") ---");
                    ventaService.obtenerDetallesServicioPorVenta(primeraVenta.getIdVenta()).forEach(System.out::println);
                } else {
                    System.out.println("\nNo hay ventas para mostrar detalles.");
                }
                System.out.println("\n--- Fin de pruebas de VentaService ---");


                // =========================================================================
                // --- PRUEBAS DE COMISION SERVICE (SOLO LECTURA) ---
                // =========================================================================
              /*  System.out.println("\n\n--- INICIANDO PRUEBAS DE COMISION SERVICE (SOLO LECTURA) ---");
                ComisionService comisionService = new ComisionService();

                System.out.println("\n--- Listando TODAS las comisiones ---");
                comisionService.getAllComisiones().forEach(System.out::println);

                UsuarioDAO usuarioDAO = new UsuarioDAO();
                Usuario vendedorUser = usuarioDAO.obtenerUsuarioPorNombreUsuario("RG");
                int idVendedor = (vendedorUser != null) ? vendedorUser.getIdUsuario() : -1;

                if (idVendedor != -1) {
                    System.out.println("\n--- Listando comisiones para el Vendedor (ID: " + idVendedor + ") ---");
                    comisionService.getComisionesByUsuario(idVendedor).forEach(System.out::println);
                } else {
                    System.out.println("\nNo se pudo obtener un ID de vendedor para probar comisiones por usuario.");
                }

                System.out.println("\n--- Listando comisiones con estado 'Pendiente' ---");
                comisionService.getComisionesByEstado("Pendiente").forEach(System.out::println);
                System.out.println("\n--- Fin de pruebas de ComisionService ---");
*/

                // =========================================================================
                // --- PRUEBAS DE CONTROL FINANCIERO SERVICE (SOLO LECTURA) ---
                // =========================================================================
                System.out.println("\n\n--- INICIANDO PRUEBAS DE CONTROL FINANCIERO SERVICE (SOLO LECTURA) ---");
                ControlFinancieroService controlFinancieroService = new ControlFinancieroService();

                System.out.println("\n--- Listando TODOS los registros financieros ---");
                controlFinancieroService.obtenerTodosLosRegistrosFinancieros().forEach(System.out::println);

                System.out.println("\n--- Listando registros de Ingreso ---");
                controlFinancieroService.obtenerRegistrosFinancierosPorTipo("Ingreso").forEach(System.out::println);

                System.out.println("\n--- Listando registros de Egreso ---");
                controlFinancieroService.obtenerRegistrosFinancierosPorTipo("Egreso").forEach(System.out::println);

                System.out.println("\n--- Cálculos Financieros (Lectura) ---");
                System.out.println("Total Ingresos: " + controlFinancieroService.calcularTotalIngresos());
                System.out.println("Total Egresos: " + controlFinancieroService.calcularTotalEgresos());
                System.out.println("Balance Total: " + controlFinancieroService.calcularBalanceTotal());
                System.out.println("\n--- Fin de pruebas de ControlFinancieroService ---");


                // =========================================================================
                // --- PRUEBAS DE MOVIMIENTO STOCK SERVICE (SOLO LECTURA) ---
                // =========================================================================
                System.out.println("\n\n--- INICIANDO PRUEBAS DE MOVIMIENTO STOCK SERVICE (SOLO LECTURA) ---");
                MovimientoStockService movimientoStockService = new MovimientoStockService();

                System.out.println("\n--- Listando TODOS los movimientos de stock ---");
                movimientoStockService.obtenerTodosLosMovimientosStock().forEach(System.out::println);

                System.out.println("\n--- Listando movimientos de stock de tipo 'Entrada' ---");
                movimientoStockService.obtenerMovimientosStockPorTipo("Entrada").forEach(System.out::println);

                System.out.println("\n--- Listando movimientos de stock de tipo 'Salida' ---");
                movimientoStockService.obtenerMovimientosStockPorTipo("Salida").forEach(System.out::println);

                ProductoDAO productoDAO_for_mov = new ProductoDAO();
                Producto llaveUniversal = productoDAO_for_mov.obtenerProductosPorNombre("Llave Universal").stream().findFirst().orElse(null);
                if (llaveUniversal != null) {
                    System.out.println("\n--- Movimientos de stock para 'Llave Universal' (ID: " + llaveUniversal.getIdProducto() + ") ---");
                    movimientoStockService.obtenerMovimientosStockPorProducto(llaveUniversal.getIdProducto()).forEach(System.out::println);
                } else {
                    System.out.println("\nProducto 'Llave Universal' no encontrado para probar movimientos de stock por producto.");
                }

                System.out.println("\n--- Fin de pruebas de MovimientoStockService ---");


                // =========================================================================
                // --- PRUEBAS DE REPORTE SERVICE (SOLO LECTURA) ---
                // =========================================================================
                System.out.println("\n\n--- INICIANDO PRUEBAS DE REPORTE SERVICE (SOLO LECTURA) ---");
                ReporteService reporteService = new ReporteService();

                System.out.println("\n--- Reporte: Todas las Ventas ---");
                reporteService.obtenerTodasLasVentasFiltradas(null, null, null).forEach(System.out::println);




                System.out.println("\n--- Reporte: Todas las Comisiones ---");
                reporteService.obtenerTodasLasComisionesFiltradas(null, null).forEach(System.out::println);

                System.out.println("\n--- Reporte: Comisiones 'Pendientes' ---");
                reporteService.obtenerTodasLasComisionesFiltradas(null, "Pendiente").forEach(System.out::println);





                System.out.println("\n--- Reporte: Productos Más Vendidos (Top 3) ---");
                reporteService.obtenerProductosMasVendidos(3).forEach((nombre, cantidad) ->
                        System.out.println("  - " + nombre + ": " + cantidad + " unidades"));

                System.out.println("\n--- Reporte: Servicios Más Vendidos (Top 3) ---");
                reporteService.obtenerServiciosMasVendidos(3).forEach((nombre, cantidad) ->
                        System.out.println("  - " + nombre + ": " + cantidad + " unidades"));

                System.out.println("\n--- Reporte: Resumen Financiero ---");
                Map<String, BigDecimal> resumenFinanciero = reporteService.obtenerResumenFinanciero();
                resumenFinanciero.forEach((k, v) -> System.out.println("  " + k + ": " + v));

                System.out.println("\n--- Fin de pruebas de ReporteService ---");


                System.out.println("\n--- FIN DE TODAS LAS PRUEBAS DE BACKEND ---");


            } else {
                System.out.println("No se pudo establecer la conexión a la base de datos.");
            }
        } catch (SQLException e) {
            System.err.println("Error general en la aplicación de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}