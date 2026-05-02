package com.controlmezcla.backend.service;

import com.controlmezcla.backend.model.Formulario;
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
import java.nio.file.Paths;
import java.util.List;

@Service
public class PdfService {

    @Value("${app.storage.pdf}")
    private String pdfPath;

    @Value("${app.storage.imagenes}")
    private String imagenesPath;

    @Value("${app.storage.firmas}")
    private String firmasPath;

    //Colores del formato
    private static final DeviceRgb AMARILLO = new DeviceRgb(255,193,7);
    private static final DeviceRgb GRIS = new DeviceRgb(80,80,80);



    private Cell crearCelda(String texto, SolidBorder borde, int colspan)
    {
        return new Cell(1, colspan)
                .add(new Paragraph(texto).setFontSize(10))
                .setBorder(borde)
                .setPadding(6);
    }

    private String valorVacio(String valor)
    {
        return valor != null ? valor: "";
    }

    private void agregarEncabezado(Document document, Formulario formulario) throws IOException
    {
        // Fila superior: Logo izquierda | Líneas decorativas derecha
        Table headerSuperior = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        headerSuperior.setWidth(UnitValue.createPercentValue(100));


        // Logo izquierda
        try {
            var recurso = getClass().getClassLoader().getResource("static/control_mezclas_logo.jpg");
            String ruta = java.net.URLDecoder.decode(recurso.getPath(), "UTF-8");
            Image logo = new Image(ImageDataFactory.create(ruta));
            logo.setWidth(100);
            headerSuperior.addCell(new Cell()
                    .add(logo)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
        } catch (Exception e) {
            headerSuperior.addCell(new Cell().setBorder(Border.NO_BORDER));
        }

        document.add(headerSuperior);
        //Codigo del informe
        document.add(new Paragraph(valorVacio(formulario.getCodigo_informe()))
                .setFontSize(16)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(5)
                .setMarginBottom(0)
                .setBold());

        // Título Informe de Servicios
        document.add(new Paragraph("Informe de Servicios")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(5)
                .setMarginBottom(5));

        // Banda gris
        try {
            var recurso = getClass().getClassLoader().getResource("static/banda_gris.png");
            String ruta = java.net.URLDecoder.decode(recurso.getPath(), "UTF-8");
            Image banda = new Image(ImageDataFactory.create(ruta));
            banda.setWidth(UnitValue.createPercentValue(100));
            document.add(banda);
        } catch (Exception e) {
            System.out.println("Error cargando banda gris: " + e.getMessage());
        }

    }

    private void agregarDatos(Document document, Formulario formulario)
    {
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        tabla.setWidth(UnitValue.createPercentValue(100));
        tabla.setMarginTop(10);

        SolidBorder borde = new SolidBorder(GRIS, 0.5f);

        //Fila 1 - Cliente y fecha
        tabla.addCell(crearCelda("Cliente: " + valorVacio(formulario.getCliente()), borde, 1));
        tabla.addCell(crearCelda("Fecha: " + (formulario.getFecha() != null ? formulario.getFecha().toString(): ""), borde, 1));


        //Fila 2 - Contacto y telefono
        tabla.addCell(crearCelda("Contacto: " + valorVacio(formulario.getContacto()), borde, 1));
        tabla.addCell(crearCelda("Telefono: " + valorVacio(formulario.getTelefono()), borde, 1));


        //Fila 3 - Direccion y Obra juntos
        String obra_completa = valorVacio(formulario.getDireccion()) + " - " + valorVacio(formulario.getObra());
        tabla.addCell(crearCelda("Obra: " + obra_completa, borde, 2));

        document.add(tabla);

        //Linea divisora
        document.add(new Paragraph("")
                .setBorderBottom(new SolidBorder(AMARILLO, 2f))
                .setMarginTop(20)
                .setMarginBottom(15));

        //Tabla de tipo y clase de mantenimiento
        Table tabla_mantenimiento = new Table(UnitValue.createPercentArray(new float[]{100}));
        tabla_mantenimiento.setWidth(UnitValue.createPercentValue(100));

        //Fila 1 - Tipo de mantenimiento
        tabla_mantenimiento.addCell(crearCelda("Tipo de mantenimiento: " + valorVacio(formulario.getTipo_mantenimiento()), borde, 1));

        //Fila 2 - Clase de mantenimiento
        tabla_mantenimiento.addCell(crearCelda("Clase de mantenimiento: " + valorVacio(formulario.getClases_mantenimiento()), borde, 1));

        document.add(tabla_mantenimiento);

    }

    private void agregarDescripcion(Document document, Formulario formulario)
    {
        document.add(new Paragraph("Descripcion del trabajo realizado")
                .setBold()
                .setFontSize(11)
                .setMarginTop(10)
        );

        Table tabla = new Table(UnitValue.createPercentArray(new float[]{100}));
        tabla.setWidth(UnitValue.createPercentValue(100));

        Cell celda = new Cell()
                .add(new Paragraph(valorVacio(formulario.getDescripcion())))
                .setMinHeight(150)
                .setBorder(new SolidBorder(GRIS, 0.5f))
                .setPadding(8);
        tabla.addCell(celda);
        document.add(tabla);

        //Materiales utilizados - Solo si hay materiales que agregar
        if(formulario.getMateriales_utilizados() != null && !formulario.getMateriales_utilizados().isEmpty())
        {
            document.add(new Paragraph("Materiales Utilizados")
                    .setBold()
                    .setFontSize(11)
                    .setMarginTop(10)
            );

            Table tabla_materiales = new Table(UnitValue.createPercentArray(new float[]{20,80}));
            tabla_materiales.setWidth(UnitValue.createPercentValue(100));
            SolidBorder borde = new SolidBorder(GRIS, 0.5f);

            //Encabezado de la tabla - CANTIDAD
            Cell cantidad = new Cell()
                    .add(new Paragraph("CANTIDAD").setBold().setFontSize(10))
                    .setBorder(borde)
                    .setPadding(6)
                    .setBackgroundColor(new DeviceRgb(220, 220, 220));

            //Encabezado de la tabla - DESCRIPCION
            Cell descripcion = new Cell()
                    .add(new Paragraph("DESCRIPCION").setBold().setFontSize(10))
                    .setBorder(borde)
                    .setPadding(6)
                    .setBackgroundColor(new DeviceRgb(220, 220, 220));

            tabla_materiales.addCell(cantidad);
            tabla_materiales.addCell(descripcion);

            //Filas de materiales
            String[] materiales = formulario.getMateriales_utilizados().split(",");

            for(String material: materiales)
            {
                tabla_materiales.addCell(new Cell()
                        .add(new Paragraph("1").setFontSize(10))
                        .setBorder(borde)
                        .setPadding(6)
                        .setMinHeight(20));

                tabla_materiales.addCell(new Cell()
                        .add(new Paragraph(material.trim()).setFontSize(10))
                        .setBorder(borde)
                        .setPadding(6)
                        .setMinHeight(20));
            }

            document.add(tabla_materiales);
        }
    }

    private void agregarFirmas(Document document, Formulario formulario, String carpeta)
    {
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        tabla.setWidth(UnitValue.createPercentValue(100));
        tabla.setMarginTop(10);

        SolidBorder borde = new SolidBorder(GRIS, 0.5f);

        //Firma cliente
        Cell celdaFirmaCliente = new Cell()
                .setBorder(borde)
                .setPadding(8)
                .setMinHeight(80);
        celdaFirmaCliente.add(new Paragraph("Firma del cliente: ").setBold());

        if (formulario.getFirma_cliente() != null)
        {
            try
            {
                Image firmaCliente = new Image(ImageDataFactory.create(
                     carpeta + "/" + formulario.getFirma_cliente()));
                firmaCliente.setWidth(150).setHeight(60);
                celdaFirmaCliente.add(firmaCliente);
            }
            catch (Exception e)
            {
                celdaFirmaCliente.add(new Paragraph(""));
            }
        }

        tabla.addCell(celdaFirmaCliente);

        //Firma tecnico
        Cell celdaFirmaTecnico = new Cell()
                .setBorder(borde)
                .setPadding(8)
                .setMinHeight(80);
        celdaFirmaTecnico.add(new Paragraph("Firma del tecnico:").setBold());

        if(formulario.getFirma_tecnico() != null)
        {
            try
            {
                Image firmaTecnico = new Image(ImageDataFactory.create(
                        carpeta + "/" + formulario.getFirma_tecnico()
                ));
                firmaTecnico.setWidth(150).setHeight(60);
                celdaFirmaTecnico.add(firmaTecnico);
            }
            catch (Exception e)
            {
                celdaFirmaTecnico.add(new Paragraph(""));
            }
        }

        tabla.addCell(celdaFirmaTecnico);
        document.add(tabla);
    }

    private void agregarImagenes(Document document, Formulario formulario, List<String> imagenes, String carpeta)
    {
        document.add(new Paragraph("Imagenes del servicio")
                .setBold()
                .setFontSize(11)
                .setMarginTop(15)
        );

        Table tabla = new Table(UnitValue.createPercentArray(new float []{50, 50}));
        tabla.setWidth(UnitValue.createPercentValue(100));

        for (String rutaImagen : imagenes)
        {
            try
            {
                Image imagen = new Image(ImageDataFactory.create(carpeta + "/" + rutaImagen));
                imagen.setWidth(230).setHeight(180);

                Cell celda = new Cell()
                        .add(imagen)
                        .setBorder(new SolidBorder(GRIS, 0.5f))
                        .setPadding(5)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);
                tabla.addCell(celda);
            }
            catch (Exception e)
            {
                tabla.addCell(new Cell()
                        .add( new Paragraph("Imagen no disponible"))
                        .setBorder(new SolidBorder(GRIS, 0.5f)
                        )
                );
            }
        }

        if(imagenes.size() % 2 != 0)
        {
            tabla.addCell(new Cell().setBorder(Border.NO_BORDER));
        }

        document.add(tabla);
    }

    public String generarPDF(Formulario formulario, List<String> imagenes, String carpeta) throws IOException
    {
        //Nombre del archivo
        String nombreArchivo = "informe_" + formulario.getId() + ".pdf";
        String rutaCompleta = pdfPath + nombreArchivo;

        //Crear el documento PDF
        Files.createDirectories(Paths.get(pdfPath));
        PdfWriter writer = new PdfWriter(rutaCompleta);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(20, 30, 20, 30);

        // 1. Encabezado
        agregarEncabezado(document, formulario);

        //2. Datos Cliente
        agregarDatos(document,formulario);

        //3. Descripcion del trabajo
        agregarDescripcion(document, formulario);

        //4. imagenes
        if(imagenes != null && !imagenes.isEmpty())
        {
            agregarImagenes(document, formulario ,imagenes, carpeta);
        }

        //5. firmas
        agregarFirmas(document, formulario, carpeta);

        document.close();
        return nombreArchivo;
    }


}
