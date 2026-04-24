package com.controlmezcla.backend.service;

import com.controlmezcla.backend.model.ActaSilo;
import com.controlmezcla.backend.model.Imagenes;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ActaSiloPdfService {

    @Value("${app.storage.pdf}")
    private String pdf_path;

    private static final DeviceRgb AMARILLO = new DeviceRgb(255, 193, 7);
    private static final DeviceRgb GRIS = new DeviceRgb(80, 80, 80);
    private static final DeviceRgb GRIS_CLARO = new DeviceRgb(220, 220, 220);

    private String valorVacio(String valor)
    {
        return valor != null ? valor: "";
    }

    private Cell crearCelda(String texto, SolidBorder borde, int colspan)
    {
        return new Cell(1, colspan)
                .add(new Paragraph(texto).setFontSize(10))
                .setBorder(borde)
                .setPadding(6);
    }

    private void agregarEncabezado(Document documento, ActaSilo acta) throws IOException
    {
        //Logo Cemex
        try
        {
            var recurso = getClass().getClassLoader().getResource("static/logo_cemex.png");
            String ruta = java.net.URLDecoder.decode(recurso.getPath(), "UTF-8");
            Image logo = new Image(ImageDataFactory.create(ruta));
            logo.setWidth(120);
            documento.add(logo);

        } catch (Exception e) {

            System.out.println("Error cargando el logo: " + e.getMessage());
        }

        //Espacio
        documento.add(new Paragraph("").setMarginTop(15));

        //Ciudad y fecha
        documento.add(new Paragraph("Ciudad: " + valorVacio(acta.getCiudad()))
                .setFontSize(15)
                .setMarginBottom(3)
        );
        documento.add(new Paragraph("Fecha: " + (acta.getFecha() != null ? acta.getFecha().toString() : ""))
                .setFontSize(11)
                .setMarginBottom(15)
        );

        //Titulo centrado
        documento.add(new Paragraph("ACTA MANTENIMIENTO DE SILO")
                .setFontSize(15)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(15)
                .setMarginBottom(20)
        );
    }

    private void agregarDatos(Document documento, ActaSilo acta)
    {
        //Texto principal del acta
        String texto_acta = String.format(
                "Con la presente se hace entrega formal al señor %s, " +
                        "identificado con C.C. %s de %s, quien actúa en representación " +
                        "del cliente %s, Obra %s, del silo número %s para almacenamiento " +
                        "de cemento, con una capacidad de %s toneladas. Operativo y con el mantenimiento de:",
                valorVacio(acta.getContacto()),
                valorVacio(acta.getCedula()),
                valorVacio(acta.getCiudad_cedula()),
                valorVacio(acta.getCliente()),
                valorVacio(acta.getObra()),
                valorVacio(acta.getNumero_silo()),
                valorVacio(acta.getNumero_toneladas())
        );

        documento.add(new Paragraph(texto_acta)
                .setFontSize(11)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginBottom(10)
        );

        //Area de descripcion del mantenimiento
        Table tabla_descripcion = new Table(UnitValue.createPercentArray(new float[]{100}));
        tabla_descripcion.setWidth(UnitValue.createPercentValue(100));
        tabla_descripcion.addCell(new Cell()
                .add(new Paragraph(valorVacio(acta.getDescripcion())))
                .setMinHeight(200)
                .setBorder(new SolidBorder(GRIS, 0.5f))
                .setPadding(8));

        documento.add(tabla_descripcion);
    }
}
