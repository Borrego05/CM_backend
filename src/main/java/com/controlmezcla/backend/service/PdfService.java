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

    private void agregarEncabezado(Document document) throws IOException
    {
        Table header = new Table(UnitValue.createPercentArray(new float []{30,70}));
        header.setWidth(UnitValue.createPercentValue(100));

        //Logo
        try
        {
            String logoPath = getClass().getClassLoader().getResource("static/logo_control_mezclas.png").getPath();
            Image logo = new Image(ImageDataFactory.create(logoPath));
            logo.setWidth(80);
            Cell celdaLogo = new Cell()
                    .add(logo)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            header.addCell(celdaLogo);
        }
        catch (Exception e)
        {
            header.addCell(new Cell().setBorder(Border.NO_BORDER));
        }

        //Titulo
        Cell celdaTitulo = new Cell()
                .add(new Paragraph("Informe de servicios")
                        .setFontSize(18)
                        .setBold()
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        header.addCell(celdaTitulo);
        document.add(header);

        //Linea separadora amarilla
        document.add(new Paragraph("")
                .setBorderBottom(new SolidBorder(AMARILLO, 3))
                .setMarginBottom(10));
    }

    private void agregarDatos(Document document, Formulario formulario)
    {
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        tabla.setWidth(UnitValue.createPercentValue(100));
        tabla.setMarginTop(10);

        SolidBorder borde = new SolidBorder(GRIS, 0.5f);

        //Fila 1 - Cliente (2 columnas)
        tabla.addCell(crearCelda("Cliente: " + valorVacio(formulario.getCliente()), borde, 2));

        //Fila 2 - Direccion y obra
        tabla.addCell(crearCelda("Direccion: " + valorVacio(formulario.getDireccion()), borde,1));
        tabla.addCell(crearCelda("Obra: " + valorVacio(formulario.getObra()), borde, 1));

        //Fila 3 - Telefono y Fecha
        tabla.addCell(crearCelda("Telefono: " + valorVacio(formulario.getTelefono()), borde, 1));
        tabla.addCell(crearCelda("Fecha: " + (formulario.getFecha() != null ? formulario.getFecha().toString(): ""), borde, 1));

        document.add(tabla);
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
        agregarEncabezado(document);

        //2. Datos Cliente
        agregarDatos(document,formulario);

        //3. Descripcion del trabajo
        agregarDescripcion(document, formulario);

        //4. firmas
        agregarFirmas(document, formulario, carpeta);

        //5. imagenes
        if(imagenes != null && !imagenes.isEmpty())
        {
            agregarImagenes(document, formulario ,imagenes, carpeta);
        }

        document.close();
        return nombreArchivo;
    }


}
