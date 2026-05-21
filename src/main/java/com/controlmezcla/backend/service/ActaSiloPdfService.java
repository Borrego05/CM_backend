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
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ActaSiloPdfService {

    // ── ALMACENAMIENTO EN DISCO (desactivado temporalmente para Railway) ──────
    // Para reactivar: descomentar esta anotación @Value y en application.properties
    // (app.storage.pdf)
    // @Value("${app.storage.pdf}")
    // private String pdf_path;                                                 // [STORAGE]

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

        // Logo izquierda — cargado desde classpath como stream (compatible con JAR/Railway)
        try (var stream = getClass().getClassLoader().getResourceAsStream("static/logo_cemex.png")) {
            if (stream != null) {
                Image logo_izquierda = new Image(ImageDataFactory.create(stream.readAllBytes()));
                logo_izquierda.setWidth(130);
                header_tabla.addCell(new Cell()
                        .add(logo_izquierda)
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setPaddingLeft(0)
                        .setPaddingRight(20)
                );
            } else {
                header_tabla.addCell(new Cell().setBorder(Border.NO_BORDER));
            }
        } catch (Exception e) {
            System.out.println("Error cargando el logo Cemex izquierda: " + e.getMessage());
            header_tabla.addCell(new Cell().setBorder(Border.NO_BORDER));
        }

        //Celda logo de la derecha
//        Cell celda_derecha = new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
//                .setVerticalAlignment(VerticalAlignment.TOP)
//                .setPaddingLeft(220)
//                .setPaddingRight(0);

        // Logo derecha — cargado desde classpath como stream (compatible con JAR/Railway)
        try (var stream = getClass().getClassLoader().getResourceAsStream("static/control_mezclas_logo.jpg")) {
            if (stream != null) {
                Image logo_derecha = new Image(ImageDataFactory.create(stream.readAllBytes()));
                logo_derecha.setWidth(120);
                logo_derecha.setHorizontalAlignment(HorizontalAlignment.RIGHT);
                header_tabla.addCell(new Cell()
                        .add(logo_derecha)
                        .setBorder(Border.NO_BORDER)
                        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );
            } else {
                header_tabla.addCell(new Cell().setBorder(Border.NO_BORDER));
            }
        } catch (Exception e) {
            System.out.println("Error cargando el logo control mezclas derecha: " + e.getMessage());
            header_tabla.addCell(new Cell().setBorder(Border.NO_BORDER));
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
                .setFontSize(18)
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

    private void AgregarImagenes(Document documento, List<byte[]> imagenesBytes)
    {
        documento.add(new Paragraph("IMAGENES DEL SERVICIO")
                .setBold()
                .setFontSize(11)
                .setMarginTop(15)
        );

        Table tabla_imagenes = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        tabla_imagenes.setWidth(UnitValue.createPercentValue(100));

        // Imágenes cargadas desde bytes en memoria (sin disco)
        for (byte[] imagen_bytes : imagenesBytes)
        {
            try
            {
                Image imagen = new Image(ImageDataFactory.create(imagen_bytes));
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

        if (imagenesBytes.size() % 2 != 0)
        {
            tabla_imagenes.addCell(new Cell().setBorder(Border.NO_BORDER));
        }

        documento.add(tabla_imagenes);
    }

    private void AgregarFirmas(Document documento, ActaSilo actaSilo, byte[] firmaClienteBytes)
    {
        Table tabla_firmas = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        tabla_firmas.setWidth(UnitValue.createPercentValue(100));
        tabla_firmas.setMarginTop(30);

        // ENTREGA
        Cell celda_entrega = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(8);

        celda_entrega.add(new Paragraph("ENTREGA")
                .setBold()
                .setFontSize(11)
        );

        celda_entrega.add(new Paragraph("Nombre: " + valorVacio(actaSilo.getNombre_tecnico()))
                .setFontSize(10));

        celda_entrega.add(new Paragraph("Celular: " + valorVacio(actaSilo.getTelefono_tecnico()))
                .setFontSize(10));

        tabla_firmas.addCell(celda_entrega);

        // RECIBE
        Cell celda_recibe = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(8);

        celda_recibe.add(new Paragraph("RECIBE")
                .setBold()
                .setFontSize(11)
        );

        celda_recibe.add(new Paragraph("Nombre: " + valorVacio(actaSilo.getNombre_recibe()))
                .setFontSize(10));

        celda_recibe.add(new Paragraph("Cédula: " + valorVacio(actaSilo.getCedula_recibe()))
                .setFontSize(10));

        // Firma del cliente — cargada desde bytes en memoria (sin disco)
        if (firmaClienteBytes != null && firmaClienteBytes.length > 0)
        {
            try
            {
                Image firmaImg = new Image(ImageDataFactory.create(firmaClienteBytes));
                firmaImg.setWidth(150).setHeight(60);
                celda_recibe.add(firmaImg);
            }
            catch (Exception e)
            {
                celda_recibe.add(new Paragraph(""));
            }
        }

        tabla_firmas.addCell(celda_recibe);

        documento.add(tabla_firmas);
    }

    // ── GENERACIÓN EN MEMORIA (activo para Railway) ───────────────────────────
    // Devuelve el PDF como byte[] sin escribir ningún archivo en disco.
    // Para volver al modo disco: cambiar la firma a `public String GenerarPdf(...)`,
    // reemplazar ByteArrayOutputStream por PdfWriter(ruta_completa), crear directorios
    // con Files.createDirectories y devolver el nombre del archivo.
    public byte[] GenerarPdf(ActaSilo acta, List<byte[]> imagenesBytes, byte[] firmaClienteBytes) throws IOException
    {
        // ── MODO DISCO (desactivado para Railway) ──────────────────────────────
        // String nombre_archivo = "informe_silo_" + acta.getId() + ".pdf";    // [STORAGE]
        // String ruta_completa = pdf_path + nombre_archivo;                   // [STORAGE]
        // Files.createDirectories(Paths.get(pdf_path));                       // [STORAGE]
        // PdfWriter writer = new PdfWriter(ruta_completa);                    // [STORAGE]
        // ── FIN MODO DISCO ─────────────────────────────────────────────────────

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf_doc = new PdfDocument(writer);
        Document documento = new Document(pdf_doc, PageSize.A4);
        documento.setMargins(40, 50, 40, 50);

        // Encabezado
        agregarEncabezado(documento, acta);

        // Párrafo
        agregarParrafos(documento, acta);

        // Imágenes desde bytes en memoria
        if (imagenesBytes != null && !imagenesBytes.isEmpty())
        {
            AgregarImagenes(documento, imagenesBytes);
        }

        // Firmas desde bytes en memoria
        AgregarFirmas(documento, acta, firmaClienteBytes);

        documento.close();

        // ── MODO DISCO (desactivado para Railway) ──────────────────────────────
        // return nombre_archivo;                                               // [STORAGE]
        // ── FIN MODO DISCO ─────────────────────────────────────────────────────
        return baos.toByteArray();
    }
    // ── FIN GENERACIÓN EN MEMORIA ─────────────────────────────────────────────
}
