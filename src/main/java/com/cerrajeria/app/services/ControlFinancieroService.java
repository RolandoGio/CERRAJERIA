package com.cerrajeria.app.services;

import com.cerrajeria.app.dao.ControlFinancieroDAO;
import com.cerrajeria.app.models.ControlFinanciero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Clase de servicio para la gestión del control financiero.
 * Contiene la lógica de negocio para registrar ingresos y egresos,
 * así como para actualizar y consultar registros financieros.
 */
public class ControlFinancieroService {

    private ControlFinancieroDAO controlFinancieroDAO;

    // Constructor
    public ControlFinancieroService() {
        this.controlFinancieroDAO = new ControlFinancieroDAO();
    }

    /**
     * Registra un nuevo ingreso en el control financiero.
     * @param descripcion Descripción del ingreso.
     * @param monto Monto del ingreso.
     * @return true si el ingreso fue registrado exitosamente, false en caso contrario.
     */
    public boolean registrarIngreso(String descripcion, BigDecimal monto) {
        if (descripcion == null || descripcion.trim().isEmpty() || monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Error al registrar ingreso: Descripción o monto inválidos. El monto debe ser positivo.");
            return false;
        }
        ControlFinanciero ingreso = new ControlFinanciero("Ingreso", descripcion, monto);
        int id = controlFinancieroDAO.insertarControlFinanciero(ingreso);
        return id != -1;
    }

    /**
     * Registra un nuevo egreso en el control financiero.
     * @param descripcion Descripción del egreso.
     * @param monto Monto del egreso.
     * @return true si el egreso fue registrado exitosamente, false en caso contrario.
     */
    public boolean registrarEgreso(String descripcion, BigDecimal monto) {
        if (descripcion == null || descripcion.trim().isEmpty() || monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Error al registrar egreso: Descripción o monto inválidos. El monto debe ser positivo.");
            return false;
        }
        ControlFinanciero egreso = new ControlFinanciero("Egreso", descripcion, monto);
        int id = controlFinancieroDAO.insertarControlFinanciero(egreso);
        return id != -1;
    }

    /**
     * Actualiza un registro financiero existente.
     * @param registro El objeto ControlFinanciero con los datos actualizados.
     * El ID debe estar establecido.
     * @return true si la actualización es exitosa, false en caso contrario.
     */
    public boolean actualizarRegistroFinanciero(ControlFinanciero registro) {
        if (registro == null || registro.getIdControlFinanciero() <= 0) {
            System.err.println("Error al actualizar registro financiero: ID de registro inválido.");
            return false;
        }
        if (registro.getDescripcion() == null || registro.getDescripcion().trim().isEmpty() ||
                registro.getMonto() == null || registro.getMonto().compareTo(BigDecimal.ZERO) <= 0 ||
                registro.getTipo() == null || (!registro.getTipo().equalsIgnoreCase("Ingreso") && !registro.getTipo().equalsIgnoreCase("Egreso"))) {
            System.err.println("Error al actualizar registro financiero: Campos obligatorios o valores inválidos.");
            return false;
        }

        return controlFinancieroDAO.actualizarControlFinanciero(registro);
    }

    /**
     * Elimina un registro financiero por su ID.
     * @param idRegistro ID del registro a eliminar.
     * @return true si la eliminación es exitosa, false en caso contrario.
     */
    public boolean eliminarRegistroFinanciero(int idRegistro) {
        if (idRegistro <= 0) {
            System.err.println("Error al eliminar registro financiero: ID de registro inválido.");
            return false;
        }
        return controlFinancieroDAO.eliminarControlFinanciero(idRegistro);
    }

    /**
     * Obtiene un registro financiero por su ID.
     * @param idRegistro ID del registro.
     * @return El objeto ControlFinanciero o null.
     */
    public ControlFinanciero obtenerRegistroFinancieroPorId(int idRegistro) {
        return controlFinancieroDAO.obtenerControlFinancieroPorId(idRegistro);
    }

    /**
     * Obtiene una lista de registros financieros por tipo ('Ingreso' o 'Egreso').
     * @param tipo El tipo de registro a buscar.
     * @return Lista de objetos ControlFinanciero que coinciden con el tipo.
     */
    public List<ControlFinanciero> obtenerRegistrosFinancierosPorTipo(String tipo) {
        if (tipo == null || (!tipo.equalsIgnoreCase("Ingreso") && !tipo.equalsIgnoreCase("Egreso"))) {
            System.err.println("Tipo de registro inválido. Debe ser 'Ingreso' o 'Egreso'.");
            return List.of(); // Retorna una lista vacía
        }
        return controlFinancieroDAO.obtenerControlFinancieroPorTipo(tipo);
    }

    /**
     * Obtiene una lista de todos los registros financieros.
     * @return Lista de objetos ControlFinanciero.
     */
    public List<ControlFinanciero> obtenerTodosLosRegistrosFinancieros() {
        return controlFinancieroDAO.obtenerTodosLosRegistrosFinancieros();
    }

    /**
     * Calcula el total de ingresos.
     * @return El total de ingresos.
     */
    public BigDecimal calcularTotalIngresos() {
        return obtenerRegistrosFinancierosPorTipo("Ingreso").stream()
                .map(ControlFinanciero::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el total de egresos.
     * @return El total de egresos.
     */
    public BigDecimal calcularTotalEgresos() {
        return obtenerRegistrosFinancierosPorTipo("Egreso").stream()
                .map(ControlFinanciero::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el balance financiero total (Ingresos - Egresos).
     * @return El balance financiero.
     */
    public BigDecimal calcularBalanceTotal() {
        return calcularTotalIngresos().subtract(calcularTotalEgresos());
    }

    /**
     * Obtiene los registros financieros filtrados por periodo (día, semana, mes, trimestre, año).
     * @param date Fecha de referencia.
     * @param tipoFiltro Tipo de filtro: "Día", "Semana", "Mes", "Trimestre" o "Año".
     * @return Lista filtrada.
     */
    public List<ControlFinanciero> obtenerRegistrosPorPeriodo(LocalDate date, String tipoFiltro) {
        if (date == null || tipoFiltro == null) {
            throw new IllegalArgumentException("La fecha y el tipo de filtro son obligatorios.");
        }

        return controlFinancieroDAO.obtenerRegistrosPorPeriodo(date, tipoFiltro);
    }

}