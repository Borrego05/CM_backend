package com.controlmezcla.backend.service;

import com.controlmezcla.backend.model.ActaSilo;
import com.controlmezcla.backend.model.Imagenes;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        //Tabla de los logos
        Table header_tabla = new Table(UnitValue.createPercentArray(new float[]{50,50}));
        header_tabla.setWidth(UnitValue.createPercentValue(100));
        header_tabla.setMarginBottom(0);

        //Logo izquierda
        try
        {
            var recurso = getClass().getClassLoader().getResource("static/logo_cemex.png");
            String ruta = java.net.URLDecoder.decode(recurso.getPath(), "UTF-8");
            Image logo_izquierda = new Image(ImageDataFactory.create(ruta));
            logo_izquierda.setWidth(130);
            header_tabla.addCell(new Cell()
                    .add(logo_izquierda)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPaddingLeft(0)
                    .setPaddingRight(20)
            );

        } catch (Exception e) {

            System.out.println("Error cargando el logo Cemex izquierda: " + e.getMessage());
        }

        //Celda logo de la derecha
//        Cell celda_derecha = new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
//                .setVerticalAlignment(VerticalAlignment.TOP)
//                .setPaddingLeft(220)
//                .setPaddingRight(0);

        //Logo de la derecha
        try
        {
            var recurso = getClass().getClassLoader().getResource("static/control_mezclas_logo.jpg");
            String ruta = java.net.URLDecoder.decode(recurso.getPath(), "UTF-8");
            Image logo_derecha = new Image(ImageDataFactory.create(ruta));
            logo_derecha.setWidth(120);
            logo_derecha.setHorizontalAlignment(HorizontalAlignment.RIGHT);

            header_tabla.addCell(new Cell()
                    .add(logo_derecha)
                    .setBorder(Border.NO_BORDER)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );
            //celda_derecha.add(logo_derecha);

        } catch (Exception e)
        {
            System.out.println("Error cargando el logo control mezclas derecha: " + e.getMessage());
        }

        header_tabla.addCell(new Cell().setBorder(Border.NO_BORDER));

        header_tabla.addCell(new Cell()
                .add(new Paragraph(valorVacio(acta.getCodigo_acta()))
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                //.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setPaddingTop(10)
        );
        //header_tabla.addCell(celda_derecha);
        documento.add(header_tabla);

        //Espacio
        documento.add(new Paragraph("").setMarginTop(15));

        //Ciudad y fecha
        documento.add(new Paragraph("Ciudad: " + valorVacio(acta.getCiudad()))
                .setFontSize(11)
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

    private void agregarParrafos(Document documento, ActaSilo acta) throws IOException
    {
        String texto_acta = String.format(
                "Con la presente se hace entrega formal al señor %s " +
                        "identificado con C.C. %s, quien actúa en representación " +
                        "del cliente %s, Obra %s, del silo número %s para almacenamiento " +
                        "de cemento, con una capacidad de %s toneladas. " +
                        "Operativo y con el mantenimiento de: ",

                valorVacio(acta.getContacto()),
                valorVacio(acta.getCedula()),
                valorVacio(acta.getCliente()),
                valorVacio(acta.getObra()),
                valorVacio(acta.getNumero_silo()),
                valorVacio(acta.getNumero_toneladas())
        );

        documento.add(new Paragraph(texto_acta)
                .setMarginTop(15)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginBottom(10)
        );

        documento.add(new Paragraph("")
                .setBorderBottom(new SolidBorder(AMARILLO, 2f))
                .setMarginTop(10)
                .setMarginBottom(15)
        );

        SolidBorder borde = new SolidBorder(GRIS, 0.5f);
        Table tabla_mantenimiento = new Table(UnitValue.createPercentArray(new float[]{100}));
        tabla_mantenimiento.setWidth(UnitValue.createPercentValue(100));
        tabla_mantenimiento.setMarginBottom(15);

        tabla_mantenimiento.addCell(new Cell(1, 1)
                .add(new Paragraph("Tipo de mantenimiento: " +
                        valorVacio(acta.getTipo_mantenimiento()))
                        .setFontSize(10))
                .setBorder(borde)
                .setPadding(6)
        );

        tabla_mantenimiento.addCell(new Cell(1, 1)
                .add(new Paragraph("Clase de mantenimiento: " +
                        valorVacio(acta.getClase_mantenimiento()))
                        .setFontSize(10))
                .setBorder(borde)
                .setPadding(6)
        );

        documento.add(tabla_mantenimiento);

        documento.add(new Paragraph("TRABAJO REALIZADO")
                .setBold()
                .setFontSize(11)
                .setMarginBottom(5)
        );

        Table tabla_descripcion = new Table(UnitValue.createPercentArray(new float[]{100}));
        tabla_descripcion.setWidth(UnitValue.createPercentValue(100));
        tabla_descripcion.addCell(new Cell()
                .add(new Paragraph(valorVacio(acta.getDescripcion())))
                .setMinHeight(200)
                .setBorder(new SolidBorder(GRIS, 0.5f))
                .setPadding(8)
        );

        documento.add(tabla_descripcion);
    }

    private void AgregarImagenes(Document documento, List<String> imagenes, String carpeta)
    {
        documento.add(new  Paragraph("IMAGENES DEL SERVICIO")
                .setBold()
                .setFontSize(11)
                .setMarginTop(15)
        );

        Table tabla_imagenes = new Table(UnitValue.createPercentArray(new float[]{50,50}));
        tabla_imagenes.setWidth(UnitValue.createPercentValue(100));

        for (String ruta_imagen : imagenes)
        {
            try
            {
                Image imagen = new Image(ImageDataFactory.create(carpeta + "/" + ruta_imagen));
                imagen.setWidth(230);
                imagen.setHeight(180);
                imagen.setAutoScale(false);
                tabla_imagenes.addCell(new Cell()
                        .add(imagen)
                        .setBorder(new SolidBorder(GRIS, 0.5f))
                        .setPadding(5)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                );
            }
            catch (Exception e)
            {
                tabla_imagenes.addCell(new Cell()
                        .add(new Paragraph("Imagen no disponible"))
                        .setBorder(new SolidBorder(GRIS, 0.5f))
                );
            }
        }

        if (imagenes.size() % 2 != 0)
        {
            tabla_imagenes.addCell(new Cell().setBorder(Border.NO_BORDER));
        }

        documento.add(tabla_imagenes);
    }

    private void AgregarFirmas(Document documento, ActaSilo actaSilo)
    {
        Table tabla_firmas = new Table(UnitValue.createPercentArray(new float[]{50,50}));
        tabla_firmas.setWidth(UnitValue.createPercentValue(100));
        tabla_firmas.setMarginTop(30);

        //ENTREGA
        Cell celda_entrega = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(8);

        celda_entrega.add(new Paragraph("ENTREGA")
                .setBold()
                .setFontSize(11)
        );

        celda_entrega.add(new Paragraph("____________________________")
                .setMarginTop(30)
                .setFontSize(10)
        );

        celda_entrega.add(new Paragraph("Nombre: " + valorVacio(actaSilo.getNombre_tecnico()))
                .setFontSize(10));

        celda_entrega.add(new Paragraph("Cedula: " + valorVacio(actaSilo.getCedula_tecnico()))
                .setFontSize(10)
        );

        tabla_firmas.addCell(celda_entrega);

        //RECIBE
        Cell celda_recibe = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(8);

        celda_recibe.add(new Paragraph("RECIBE")
                .setBold()
                .setFontSize(11)
        );

        celda_recibe.add(new Paragraph("____________________________")
                .setMarginTop(30)
                .setFontSize(10)
        );

        celda_recibe.add(new Paragraph("Nombre: ").setFontSize(10));

        celda_recibe.add(new Paragraph("Cedula: ").setFontSize(10));

        tabla_firmas.addCell(celda_recibe);

        documento.add(tabla_firmas);
    }

    public String GenerarPdf(ActaSilo acta, List<String> imagenes, String carpeta) throws  IOException
    {
        String nombre_archivo = "informe_silo_" + acta.getId() + ".pdf";
        String ruta_completa = pdf_path + nombre_archivo;

        Files.createDirectories(Paths.get(pdf_path));
        PdfWriter writer = new PdfWriter(ruta_completa);
        PdfDocument pdf_doc = new PdfDocument(writer);
        Document documento = new Document(pdf_doc, PageSize.A4);
        documento.setMargins(40, 50, 40, 50);

        //Encabezado
        agregarEncabezado(documento, acta);

        //Parrafo
        agregarParrafos(documento, acta);

        //Imagenes
        if(imagenes != null && !imagenes.isEmpty())
        {
            AgregarImagenes(documento, imagenes, carpeta);
        }

        //Firmas
        AgregarFirmas(documento, acta);

        documento.close();

        return nombre_archivo;
    }
}
