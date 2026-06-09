package com.controlmezcla.backend.service;

import com.controlmezcla.backend.model.ActaSilo;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BorderRadius;
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

    private static final DeviceRgb AMARILLO       = new DeviceRgb(255, 193, 7);
    private static final DeviceRgb AMARILLO_SUAVE = new DeviceRgb(255, 248, 220);
    private static final DeviceRgb GRIS_OSCURO    = new DeviceRgb(50, 50, 50);
    private static final DeviceRgb GRIS_BORDE     = new DeviceRgb(200, 200, 200);
    private static final DeviceRgb GRIS_LABEL     = new DeviceRgb(120, 120, 120);
    private static final DeviceRgb BLANCO         = new DeviceRgb(255, 255, 255);

    private String valorVacio(String valor) {
        return valor != null ? valor : "";
    }

    // Carga un recurso del classpath como bytes (devuelve null si no existe)
    private byte[] cargarRecurso(String ruta) {
        try (var stream = getClass().getClassLoader().getResourceAsStream(ruta)) {
            return stream != null ? stream.readAllBytes() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ── Banner superior: barra oscura con acento amarillo diagonal ────────────
    private static class BannerSuperior implements IEventHandler {
        private static final float BAR_H                 = 14f;
        private static final float AMARILLO_TOP_X_OFFSET = 60f;
        private static final float AMARILLO_BOT_X_OFFSET = 92f;

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfPage     page = docEvent.getPage();
            PdfDocument pdf  = docEvent.getDocument();
            Rectangle   size = page.getPageSize();
            float w = size.getWidth();
            float h = size.getHeight();

            try {
                PdfCanvas canvas = new PdfCanvas(
                        page.newContentStreamBefore(),
                        page.getResources(),
                        pdf);

                canvas.saveState();

                // 1 — Barra oscura completa
                canvas.setFillColor(new DeviceRgb(50, 50, 50));
                canvas.rectangle(0, h - BAR_H, w, BAR_H);
                canvas.fill();

                // 2 — Acento amarillo (paralelogramo con borde diagonal)
                canvas.setFillColor(new DeviceRgb(255, 193, 7));
                canvas.moveTo(w - AMARILLO_TOP_X_OFFSET, h);
                canvas.lineTo(w,                          h);
                canvas.lineTo(w,                          h - BAR_H);
                canvas.lineTo(w - AMARILLO_BOT_X_OFFSET, h - BAR_H);
                canvas.closePath();
                canvas.fill();

                canvas.restoreState();
                canvas.release();
            } catch (Exception ignored) { }
        }
    }

    // ── Encabezado de sección: [cuadro amarillo (+ icono)] [TÍTULO ──────────] ─
    private void agregarTituloSeccion(Document document, String titulo, String iconoRuta) {
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{5, 95}));
        tabla.setWidth(UnitValue.createPercentValue(100));
        tabla.setMarginTop(14).setMarginBottom(6);

        Div divAmarillo = new Div()
                .setBackgroundColor(AMARILLO)
                .setBorderRadius(new BorderRadius(6f))
                .setPadding(4);

        if (iconoRuta != null) {
            byte[] icoBytes = cargarRecurso(iconoRuta);
            if (icoBytes != null) {
                try {
                    Image ico = new Image(ImageDataFactory.create(icoBytes));
                    ico.setWidth(14).setHeight(14)
                       .setHorizontalAlignment(HorizontalAlignment.CENTER);
                    divAmarillo.add(ico);
                } catch (Exception ignored) {}
            }
        }

        tabla.addCell(new Cell()
                .add(divAmarillo)
                .setBorder(Border.NO_BORDER)
                .setPadding(0)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));

        tabla.addCell(new Cell()
                .add(new Paragraph(titulo)
                        .setBold().setFontSize(10).setFontColor(GRIS_OSCURO).setMarginBottom(0))
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(GRIS_BORDE, 1f))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingLeft(10).setPaddingBottom(4));

        document.add(tabla);
    }

    // ── Chip con borde izquierdo amarillo ─────────────────────────────────────
    private Div crearChip(String texto) {
        return new Div()
                .add(new Paragraph(texto.trim().toUpperCase())
                        .setFontSize(9).setFontColor(GRIS_OSCURO).setMarginBottom(0))
                .setBorderLeft(new SolidBorder(AMARILLO, 4f))
                .setBackgroundColor(AMARILLO_SUAVE)
                .setPaddingLeft(8).setPaddingRight(8).setPaddingTop(4).setPaddingBottom(4)
                .setMarginBottom(4);
    }

    private void agregarEncabezado(Document documento, ActaSilo acta) throws IOException {
        Table header_tabla = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        header_tabla.setWidth(UnitValue.createPercentValue(100));
        header_tabla.setMarginBottom(0);

        // Logo izquierda (Cemex)
        try (var stream = getClass().getClassLoader().getResourceAsStream("static/logo_cemex.png")) {
            if (stream != null) {
                Image logo_izquierda = new Image(ImageDataFactory.create(stream.readAllBytes()));
                logo_izquierda.setWidth(130);
                header_tabla.addCell(new Cell()
                        .add(logo_izquierda)
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setPaddingLeft(0).setPaddingRight(20));
            } else {
                header_tabla.addCell(new Cell().setBorder(Border.NO_BORDER));
            }
        } catch (Exception e) {
            header_tabla.addCell(new Cell().setBorder(Border.NO_BORDER));
        }

        // Logo a la derecha (Control Mezclas)
        try (var stream = getClass().getClassLoader().getResourceAsStream("static/control_mezclas_logo.jpg")) {
            if (stream != null) {
                Image logo_derecha = new Image(ImageDataFactory.create(stream.readAllBytes()));
                logo_derecha.setWidth(120);
                logo_derecha.setHorizontalAlignment(HorizontalAlignment.RIGHT);
                header_tabla.addCell(new Cell()
                        .add(logo_derecha)
                        .setBorder(Border.NO_BORDER)
                        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
            } else {
                header_tabla.addCell(new Cell().setBorder(Border.NO_BORDER));
            }
        } catch (Exception e) {
            header_tabla.addCell(new Cell().setBorder(Border.NO_BORDER));
        }

        header_tabla.addCell(new Cell().setBorder(Border.NO_BORDER));
        header_tabla.addCell(new Cell()
                .add(new Paragraph(valorVacio(acta.getCodigo_acta()))
                        .setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(10));
        documento.add(header_tabla);

        documento.add(new Paragraph("").setMarginTop(15));

        documento.add(new Paragraph("Ciudad: " + valorVacio(acta.getCiudad()))
                .setFontSize(11).setMarginBottom(3));
        documento.add(new Paragraph("Fecha: " + (acta.getFecha() != null ? acta.getFecha().toString() : ""))
                .setFontSize(11).setMarginBottom(15));

        documento.add(new Paragraph("ACTA MANTENIMIENTO DE SILO")
                .setFontSize(18).setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(15).setMarginBottom(20));
    }

    private void agregarParrafos(Document documento, ActaSilo acta) {
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
                .setMarginBottom(10));

        documento.add(new Paragraph("")
                .setBorderBottom(new SolidBorder(AMARILLO, 2f))
                .setMarginTop(10).setMarginBottom(15));

        // ── DETALLES DEL MANTENIMIENTO ────────────────────────────────────────
        agregarTituloSeccion(documento, "DETALLES DEL MANTENIMIENTO", "static/herramienta.png");

        Table tablaMantenimiento = new Table(UnitValue.createPercentArray(new float[]{28, 72}));
        tablaMantenimiento.setWidth(UnitValue.createPercentValue(100));
        SolidBorder borde = new SolidBorder(GRIS_BORDE, 0.8f);

        // Fila: Tipo de mantenimiento
        tablaMantenimiento.addCell(new Cell()
                .add(new Paragraph("TIPO DE\nMANTENIMIENTO").setFontSize(8).setBold().setFontColor(GRIS_OSCURO))
                .setBorder(borde).setPadding(10).setVerticalAlignment(VerticalAlignment.MIDDLE));

        Cell celdaTipos = new Cell().setBorder(borde).setPadding(10).setVerticalAlignment(VerticalAlignment.MIDDLE);
        String tipoCSV = valorVacio(acta.getTipo_mantenimiento());
        if (!tipoCSV.isEmpty()) {
            for (String tipo : tipoCSV.split(",")) {
                celdaTipos.add(crearChip(tipo));
            }
        }
        tablaMantenimiento.addCell(celdaTipos);

        // Fila: Clase de mantenimiento
        tablaMantenimiento.addCell(new Cell()
                .add(new Paragraph("CLASE DE\nMANTENIMIENTO").setFontSize(8).setBold().setFontColor(GRIS_OSCURO))
                .setBorder(borde).setPadding(10).setVerticalAlignment(VerticalAlignment.MIDDLE));

        Cell celdaClases = new Cell().setBorder(borde).setPadding(10).setVerticalAlignment(VerticalAlignment.MIDDLE);
        String claseCSV = valorVacio(acta.getClase_mantenimiento());
        if (!claseCSV.isEmpty()) {
            for (String clase : claseCSV.split(",")) {
                celdaClases.add(crearChip(clase));
            }
        }
        tablaMantenimiento.addCell(celdaClases);

        documento.add(tablaMantenimiento);

        // ── TRABAJO REALIZADO ─────────────────────────────────────────────────
        agregarTituloSeccion(documento, "TRABAJO REALIZADO", "static/doc.png");

        Table tabla_descripcion = new Table(UnitValue.createPercentArray(new float[]{100}));
        tabla_descripcion.setWidth(UnitValue.createPercentValue(100));
        tabla_descripcion.addCell(new Cell()
                .add(new Paragraph(valorVacio(acta.getDescripcion())).setFontSize(10))
                .setMinHeight(150)
                .setBorder(new SolidBorder(GRIS_BORDE, 0.8f))
                .setPadding(10));
        documento.add(tabla_descripcion);
    }

    private void AgregarImagenes(Document documento, List<byte[]> imagenesBytes) {
        // ── IMÁGENES DEL SERVICIO ─────────────────────────────────────────────
        agregarTituloSeccion(documento, "IMÁGENES DEL SERVICIO", "static/camara.png");

        // 4 columnas igual que en Informe de Servicios
        Table tabla_imagenes = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}));
        tabla_imagenes.setWidth(UnitValue.createPercentValue(100));

        for (byte[] imagen_bytes : imagenesBytes) {
            try {
                Image imagen = new Image(ImageDataFactory.create(imagen_bytes));
                imagen.setWidth(118).setHeight(100);
                tabla_imagenes.addCell(new Cell()
                        .add(imagen)
                        .setBorder(new SolidBorder(GRIS_BORDE, 0.5f))
                        .setPadding(4)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER));
            } catch (Exception e) {
                tabla_imagenes.addCell(new Cell()
                        .add(new Paragraph("Imagen no disponible").setFontSize(8))
                        .setBorder(new SolidBorder(GRIS_BORDE, 0.5f)));
            }
        }

        // Rellenar celdas vacías para completar la última fila
        int remainder = imagenesBytes.size() % 4;
        if (remainder != 0) {
            for (int i = 0; i < 4 - remainder; i++) {
                tabla_imagenes.addCell(new Cell().setBorder(Border.NO_BORDER));
            }
        }

        documento.add(tabla_imagenes);
    }

    private void agregarPie(Document document) {
        document.add(new Paragraph("").setMarginTop(20));

        Table footer = new Table(UnitValue.createPercentArray(new float[]{33, 34, 33}));
        footer.setWidth(UnitValue.createPercentValue(100));

        SolidBorder separadorPie = new SolidBorder(new DeviceRgb(100, 100, 100), 1f);

        footer.addCell(new Cell()
                .add(new Paragraph("Servicio tecnico con calidad,\nseguridad y compromiso.")
                        .setFontSize(8).setFontColor(BLANCO))
                .setBackgroundColor(GRIS_OSCURO)
                .setBorder(Border.NO_BORDER)
                .setBorderRight(separadorPie)
                .setPadding(12));

        footer.addCell(new Cell()
                .add(new Paragraph("+57 320 487 0078\nproduccion@controlmezclas.com")
                        .setFontSize(8).setFontColor(BLANCO).setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(GRIS_OSCURO)
                .setBorder(Border.NO_BORDER)
                .setBorderRight(separadorPie)
                .setPadding(12)
                .setTextAlignment(TextAlignment.CENTER));

        footer.addCell(new Cell()
                .add(new Paragraph("www.controlmezclas.com")
                        .setFontSize(8).setFontColor(BLANCO).setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(GRIS_OSCURO)
                .setBorder(Border.NO_BORDER)
                .setBorderRight(separadorPie)
                .setPadding(12)
                .setTextAlignment(TextAlignment.RIGHT));

        document.add(footer);
    }

    private void AgregarFirmas(Document documento, ActaSilo actaSilo, byte[] firmaClienteBytes) {
        Table tabla_firmas = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        tabla_firmas.setWidth(UnitValue.createPercentValue(100));
        tabla_firmas.setMarginTop(20);

        SolidBorder borde = new SolidBorder(GRIS_BORDE, 0.8f);

        // ENTREGA
        Cell celda_entrega = new Cell().setBorder(borde).setPadding(8);
        celda_entrega.add(new Paragraph("ENTREGA")
                .setBold().setFontSize(9).setFontColor(GRIS_OSCURO).setMarginBottom(6));
        celda_entrega.add(new Paragraph("Nombre: " + valorVacio(actaSilo.getNombre_tecnico()))
                .setFontSize(9).setMarginTop(4));
        celda_entrega.add(new Paragraph("Celular: " + valorVacio(actaSilo.getTelefono_tecnico()))
                .setFontSize(9));
        tabla_firmas.addCell(celda_entrega);

        // RECIBE
        Cell celda_recibe = new Cell().setBorder(borde).setPadding(8);
        celda_recibe.add(new Paragraph("RECIBE")
                .setBold().setFontSize(9).setFontColor(GRIS_OSCURO).setMarginBottom(6));
        celda_recibe.add(new Paragraph("Nombre: " + valorVacio(actaSilo.getNombre_recibe()))
                .setFontSize(9).setMarginTop(4));
        celda_recibe.add(new Paragraph("Cédula: " + valorVacio(actaSilo.getCedula_recibe()))
                .setFontSize(9));

        if (firmaClienteBytes != null && firmaClienteBytes.length > 0) {
            try {
                Image firmaImg = new Image(ImageDataFactory.create(firmaClienteBytes));
                firmaImg.setWidth(120).setHeight(48);
                celda_recibe.add(firmaImg);
            } catch (Exception e) {
                celda_recibe.add(new Paragraph(""));
            }
        }
        tabla_firmas.addCell(celda_recibe);

        documento.add(tabla_firmas);

        agregarCalificacion(documento, actaSilo);
        agregarPie(documento);
    }

    private void agregarCalificacion(Document documento, ActaSilo acta) {
        if (acta.getCalificacion() == null && (acta.getComentario_calificacion() == null || acta.getComentario_calificacion().isEmpty())) {
            return;
        }

        agregarTituloSeccion(documento, "CALIFICA NUESTRO SERVICIO", "static/estrella.png");

        Table tabla = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        tabla.setWidth(UnitValue.createPercentValue(100));
        SolidBorder borde = new SolidBorder(GRIS_BORDE, 0.8f);

        // Celda de estrellas
        Cell celdaEstrellas = new Cell().setBorder(borde).setPadding(10).setVerticalAlignment(VerticalAlignment.MIDDLE);
        celdaEstrellas.add(new Paragraph("CALIFICACIÓN").setFontSize(7).setFontColor(GRIS_LABEL).setMarginBottom(6));
        StringBuilder estrellas = new StringBuilder();
        int cal = acta.getCalificacion() != null ? acta.getCalificacion() : 0;
        for (int i = 1; i <= 5; i++) {
            estrellas.append(i <= cal ? "★" : "☆");
        }
        celdaEstrellas.add(new Paragraph(estrellas.toString()).setFontSize(22).setFontColor(AMARILLO));
        tabla.addCell(celdaEstrellas);

        // Celda de comentario
        Cell celdaComentario = new Cell().setBorder(borde).setPadding(10).setVerticalAlignment(VerticalAlignment.MIDDLE);
        celdaComentario.add(new Paragraph("COMENTARIOS").setFontSize(7).setFontColor(GRIS_LABEL).setMarginBottom(6));
        String comentario = acta.getComentario_calificacion() != null ? acta.getComentario_calificacion() : "";
        celdaComentario.add(new Paragraph(comentario).setFontSize(10));
        tabla.addCell(celdaComentario);

        documento.add(tabla);
    }

    // ── GENERACIÓN EN MEMORIA (activo para Railway) ───────────────────────────
    public byte[] GenerarPdf(ActaSilo acta, List<byte[]> imagenesBytes, byte[] firmaClienteBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf_doc = new PdfDocument(writer);

        // Registrar banner superior (negro + diagonal amarilla) en cada página
        pdf_doc.addEventHandler(PdfDocumentEvent.START_PAGE, new BannerSuperior());

        Document documento = new Document(pdf_doc, PageSize.A4);
        documento.setMargins(28, 30, 20, 30);

        agregarEncabezado(documento, acta);
        agregarParrafos(documento, acta);

        if (imagenesBytes != null && !imagenesBytes.isEmpty()) {
            AgregarImagenes(documento, imagenesBytes);
        }

        AgregarFirmas(documento, acta, firmaClienteBytes);

        documento.close();
        return baos.toByteArray();
    }
    // ── FIN GENERACIÓN EN MEMORIA ─────────────────────────────────────────────
}
