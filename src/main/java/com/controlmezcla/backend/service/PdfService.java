package com.controlmezcla.backend.service;

import com.controlmezcla.backend.model.Formulario;
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
public class PdfService {

    // ── ALMACENAMIENTO EN DISCO (desactivado temporalmente para Railway) ──────
    // @Value("${app.storage.pdf}")       private String pdfPath;
    // @Value("${app.storage.imagenes}")  private String imagenesPath;
    // @Value("${app.storage.firmas}")    private String firmasPath;

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

    // Banner superior: barra oscura con acento amarillo diagonal (derecha)
    private static class BannerSuperior implements IEventHandler {
        private static final float BAR_H   = 14f;
        private static final float AMARILLO_TOP_X_OFFSET = 60f;   // cuánto ocupa el amarillo en la parte superior
        private static final float AMARILLO_BOT_X_OFFSET = 92f;   // cuánto ocupa en la parte inferior (más ancho = diagonal más pronunciada)

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfPage   page   = docEvent.getPage();
            PdfDocument pdf  = docEvent.getDocument();
            Rectangle  size  = page.getPageSize();
            float w = size.getWidth();
            float h = size.getHeight();

            try {
                PdfCanvas canvas = new PdfCanvas(
                        page.newContentStreamBefore(),
                        page.getResources(),
                        pdf);

                // Aislar cambios de color para no afectar el stream principal del documento
                canvas.saveState();

                // 1 — Barra oscura completa
                canvas.setFillColor(new DeviceRgb(50, 50, 50));
                canvas.rectangle(0, h - BAR_H, w, BAR_H);
                canvas.fill();

                // 2 — Acento amarillo (paralelogramo con borde diagonal)
                canvas.setFillColor(new DeviceRgb(255, 193, 7));
                canvas.moveTo(w - AMARILLO_TOP_X_OFFSET, h);           // esquina superior-izquierda del amarillo
                canvas.lineTo(w,                          h);           // esquina superior-derecha
                canvas.lineTo(w,                          h - BAR_H);  // esquina inferior-derecha
                canvas.lineTo(w - AMARILLO_BOT_X_OFFSET, h - BAR_H);  // esquina inferior-izquierda (más a la izquierda → diagonal)
                canvas.closePath();
                canvas.fill();

                // Restaurar estado gráfico — el stream siguiente hereda color negro por defecto
                canvas.restoreState();

                canvas.release();
            } catch (Exception ignored) { }
        }
    }

    // Encabezado de sección: [cuadro amarillo (+ icono opcional)] [TÍTULO ─────────────────]
    private void agregarTituloSeccion(Document document, String titulo, String iconoRuta) {
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{5, 95}));
        tabla.setWidth(UnitValue.createPercentValue(100));
        tabla.setMarginTop(14).setMarginBottom(6);

        // Div amarillo con esquinas redondeadas (radio 8 pt ≈ 20°)
        Div divAmarillo = new Div()
                .setBackgroundColor(AMARILLO)
                .setBorderRadius(new BorderRadius(8f))
                .setMinHeight(28)
                .setPaddingTop(5).setPaddingBottom(5)
                .setPaddingLeft(4).setPaddingRight(4);

        if (iconoRuta != null) {
            byte[] icoBytes = cargarRecurso(iconoRuta);
            if (icoBytes != null) {
                try {
                    Image ico = new Image(ImageDataFactory.create(icoBytes));
                    ico.setWidth(18).setHeight(18)
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

    // Chip con borde izquierdo amarillo para tipos/clases seleccionados
    private Div crearChip(String texto) {
        return new Div()
                .add(new Paragraph(texto.trim().toUpperCase())
                        .setFontSize(9).setFontColor(GRIS_OSCURO).setMarginBottom(0))
                .setBorderLeft(new SolidBorder(AMARILLO, 4f))
                .setBackgroundColor(AMARILLO_SUAVE)
                .setPaddingLeft(8).setPaddingRight(8).setPaddingTop(4).setPaddingBottom(4)
                .setMarginBottom(4);
    }

    private void agregarEncabezado(Document document, Formulario formulario) throws IOException {
        // Fila principal: Logo (izquierda) | Código + Fecha (derecha)
        Table header = new Table(UnitValue.createPercentArray(new float[]{55, 45}));
        header.setWidth(UnitValue.createPercentValue(100));

        // Logo — sin modificar, se carga igual que antes
        Cell celdaLogo = new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        try (var stream = getClass().getClassLoader().getResourceAsStream("static/control_mezclas_logo.jpg")) {
            if (stream != null) {
                Image logo = new Image(ImageDataFactory.create(stream.readAllBytes()));
                logo.setWidth(150);
                celdaLogo.add(logo);
            } else {
                celdaLogo.add(new Paragraph(""));
            }
        } catch (Exception e) {
            celdaLogo.add(new Paragraph(""));
        }
        header.addCell(celdaLogo);

        // Código de servicio y Fecha: [ico_codigo][texto código]|[ico_fecha][texto fecha]
        // Layout sin recuadros — separados por una línea vertical gris
        Table tablaCodigoFecha = new Table(UnitValue.createPercentArray(new float[]{16, 36, 16, 32}));
        tablaCodigoFecha.setWidth(UnitValue.createPercentValue(100));

        // ── Icono clipboard (código) ─────────────────────────────────────────
        Cell icoCodigoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setPadding(4);
        byte[] icoCodigoBytes = cargarRecurso("static/logo_numeral.png");
        if (icoCodigoBytes != null) {
            try {
                Image icoImg = new Image(ImageDataFactory.create(icoCodigoBytes));
                icoImg.setWidth(30).setHeight(30);
                icoCodigoCell.add(icoImg);
            } catch (Exception ignored) {}
        }
        tablaCodigoFecha.addCell(icoCodigoCell);

        // ── Texto: CÓDIGO DE SERVICIO + valor (borde derecho = línea separadora gris) ──
        Cell celdaCodigo = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderRight(new SolidBorder(GRIS_BORDE, 1.5f))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(2).setPaddingRight(10);
        celdaCodigo.add(new Paragraph("CÓDIGO DE SERVICIO")
                .setFontSize(7).setFontColor(GRIS_LABEL).setMarginBottom(2));
        celdaCodigo.add(new Paragraph(valorVacio(formulario.getCodigo_informe()))
                .setFontSize(16).setBold().setFontColor(GRIS_OSCURO));
        tablaCodigoFecha.addCell(celdaCodigo);

        // ── Icono calendario (fecha) ─────────────────────────────────────────
        Cell icoFechaCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setPaddingLeft(10).setPaddingRight(4);
        byte[] icoFechaBytes = cargarRecurso("static/logo_calendario.png");
        if (icoFechaBytes != null) {
            try {
                Image icoImg = new Image(ImageDataFactory.create(icoFechaBytes));
                icoImg.setWidth(30).setHeight(30);
                icoFechaCell.add(icoImg);
            } catch (Exception ignored) {}
        }
        tablaCodigoFecha.addCell(icoFechaCell);

        // ── Texto: FECHA + valor ─────────────────────────────────────────────
        Cell celdaFecha = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(2).setPaddingRight(4);
        celdaFecha.add(new Paragraph("FECHA")
                .setFontSize(7).setFontColor(GRIS_LABEL).setMarginBottom(2));
        celdaFecha.add(new Paragraph(formulario.getFecha() != null ? formulario.getFecha().toString() : "")
                .setFontSize(11).setBold().setFontColor(GRIS_OSCURO));
        tablaCodigoFecha.addCell(celdaFecha);

        header.addCell(new Cell()
                .add(tablaCodigoFecha)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));
        document.add(header);

        // Línea separadora amarilla
        document.add(new Paragraph("")
                .setBorderBottom(new SolidBorder(AMARILLO, 2.5f))
                .setMarginTop(8).setMarginBottom(2));
    }

    private void agregarDatos(Document document, Formulario formulario) {
        // ── 1. DATOS DEL CLIENTE ─────────────────────────────────────────────
        agregarTituloSeccion(document, "1. DATOS DEL CLIENTE", "static/persona.png");

        Table tarjetas = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        tarjetas.setWidth(UnitValue.createPercentValue(100));
        tarjetas.setMarginBottom(4);

        SolidBorder bordeTarjeta = new SolidBorder(GRIS_BORDE, 0.8f);

        Cell cardCliente = new Cell().setBorder(bordeTarjeta).setPadding(10);
        cardCliente.add(new Paragraph("CLIENTE").setFontSize(7).setFontColor(GRIS_LABEL).setMarginBottom(2));
        cardCliente.add(new Paragraph(valorVacio(formulario.getCliente())).setFontSize(10).setBold());
        tarjetas.addCell(cardCliente);

        Cell cardContacto = new Cell().setBorder(bordeTarjeta).setPadding(10);
        cardContacto.add(new Paragraph("CONTACTO").setFontSize(7).setFontColor(GRIS_LABEL).setMarginBottom(2));
        cardContacto.add(new Paragraph(valorVacio(formulario.getContacto())).setFontSize(10).setBold());
        tarjetas.addCell(cardContacto);

        Cell cardTelefono = new Cell().setBorder(bordeTarjeta).setPadding(10);
        cardTelefono.add(new Paragraph("TELÉFONO").setFontSize(7).setFontColor(GRIS_LABEL).setMarginBottom(2));
        cardTelefono.add(new Paragraph(valorVacio(formulario.getTelefono())).setFontSize(10).setBold());
        tarjetas.addCell(cardTelefono);

        String obraCompleta = valorVacio(formulario.getDireccion()) + " - " + valorVacio(formulario.getObra());
        Cell cardObra = new Cell().setBorder(bordeTarjeta).setPadding(10);
        cardObra.add(new Paragraph("OBRA").setFontSize(7).setFontColor(GRIS_LABEL).setMarginBottom(2));
        cardObra.add(new Paragraph(obraCompleta).setFontSize(10).setBold());
        tarjetas.addCell(cardObra);

        document.add(tarjetas);

        // ── 2. DETALLES DEL MANTENIMIENTO ────────────────────────────────────
        agregarTituloSeccion(document, "2. DETALLES DEL MANTENIMIENTO", "static/herramienta.png");

        Table tablaMantenimiento = new Table(UnitValue.createPercentArray(new float[]{28, 72}));
        tablaMantenimiento.setWidth(UnitValue.createPercentValue(100));
        SolidBorder borde = new SolidBorder(GRIS_BORDE, 0.8f);

        // Fila: Tipo de mantenimiento
        tablaMantenimiento.addCell(new Cell()
                .add(new Paragraph("TIPO DE\nMANTENIMIENTO").setFontSize(8).setBold().setFontColor(GRIS_OSCURO))
                .setBorder(borde).setPadding(10).setVerticalAlignment(VerticalAlignment.MIDDLE));

        Cell celdaTipos = new Cell().setBorder(borde).setPadding(10).setVerticalAlignment(VerticalAlignment.MIDDLE);
        String tiposCSV = valorVacio(formulario.getTipo_mantenimiento());
        if (!tiposCSV.isEmpty()) {
            for (String tipo : tiposCSV.split(",")) {
                celdaTipos.add(crearChip(tipo));
            }
        }
        tablaMantenimiento.addCell(celdaTipos);

        // Fila: Clase de mantenimiento
        tablaMantenimiento.addCell(new Cell()
                .add(new Paragraph("CLASE DE\nMANTENIMIENTO").setFontSize(8).setBold().setFontColor(GRIS_OSCURO))
                .setBorder(borde).setPadding(10).setVerticalAlignment(VerticalAlignment.MIDDLE));

        Cell celdaClases = new Cell().setBorder(borde).setPadding(10).setVerticalAlignment(VerticalAlignment.MIDDLE);
        String clasesCSV = valorVacio(formulario.getClases_mantenimiento());
        if (!clasesCSV.isEmpty()) {
            for (String clase : clasesCSV.split(",")) {
                celdaClases.add(crearChip(clase));
            }
        }
        tablaMantenimiento.addCell(celdaClases);

        document.add(tablaMantenimiento);
    }

    private void agregarDescripcion(Document document, Formulario formulario) {
        // ── 3. DESCRIPCIÓN DEL TRABAJO REALIZADO ─────────────────────────────
        agregarTituloSeccion(document, "3. DESCRIPCIÓN DEL TRABAJO REALIZADO", "static/doc.png");

        // Mismo cuadro grande, con el nuevo estilo visual
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{100}));
        tabla.setWidth(UnitValue.createPercentValue(100));
        tabla.setMarginBottom(4);

        tabla.addCell(new Cell()
                .add(new Paragraph(valorVacio(formulario.getDescripcion())).setFontSize(10))
                .setMinHeight(150)
                .setBorder(new SolidBorder(GRIS_BORDE, 0.8f))
                .setPadding(10));
        document.add(tabla);

        // ── 4. MATERIALES UTILIZADOS ─────────────────────────────────────────
        if (formulario.getMateriales_utilizados() != null && !formulario.getMateriales_utilizados().isEmpty()) {
            agregarTituloSeccion(document, "4. MATERIALES UTILIZADOS", "static/caja.png");

            Table tablaMateriales = new Table(UnitValue.createPercentArray(new float[]{20, 80}));
            tablaMateriales.setWidth(UnitValue.createPercentValue(100));
            SolidBorder borde = new SolidBorder(GRIS_BORDE, 0.8f);

            // Encabezado oscuro
            tablaMateriales.addCell(new Cell()
                    .add(new Paragraph("CANTIDAD").setBold().setFontSize(10)
                            .setFontColor(BLANCO).setTextAlignment(TextAlignment.CENTER))
                    .setBorder(borde).setPadding(8).setBackgroundColor(GRIS_OSCURO)
                    .setTextAlignment(TextAlignment.CENTER));

            tablaMateriales.addCell(new Cell()
                    .add(new Paragraph("DESCRIPCIÓN").setBold().setFontSize(10)
                            .setFontColor(BLANCO).setTextAlignment(TextAlignment.CENTER))
                    .setBorder(borde).setPadding(8).setBackgroundColor(GRIS_OSCURO)
                    .setTextAlignment(TextAlignment.CENTER));

            for (String material : formulario.getMateriales_utilizados().split(",")) {
                tablaMateriales.addCell(new Cell()
                        .add(new Paragraph("1").setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                        .setBorder(borde).setPadding(8).setTextAlignment(TextAlignment.CENTER));

                tablaMateriales.addCell(new Cell()
                        .add(new Paragraph(material.trim()).setFontSize(10))
                        .setBorder(borde).setPadding(8));
            }
            document.add(tablaMateriales);
        }
    }

    private void agregarImagenes(Document document, Formulario formulario, List<byte[]> imagenesBytes) {
        // ── 5. IMÁGENES DEL SERVICIO ─────────────────────────────────────────
        agregarTituloSeccion(document, "5. IMÁGENES DEL SERVICIO", "static/camara.pnggi");

        // 4 columnas igual que en la imagen de referencia
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}));
        tabla.setWidth(UnitValue.createPercentValue(100));

        for (byte[] imagenBytes : imagenesBytes) {
            try {
                Image imagen = new Image(ImageDataFactory.create(imagenBytes));
                imagen.setWidth(118).setHeight(100);
                tabla.addCell(new Cell()
                        .add(imagen)
                        .setBorder(new SolidBorder(GRIS_BORDE, 0.5f))
                        .setPadding(4)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER));
            } catch (Exception e) {
                tabla.addCell(new Cell()
                        .add(new Paragraph("Imagen no disponible").setFontSize(8))
                        .setBorder(new SolidBorder(GRIS_BORDE, 0.5f)));
            }
        }

        // Rellenar celdas vacías para completar la última fila
        int remainder = imagenesBytes.size() % 4;
        if (remainder != 0) {
            for (int i = 0; i < 4 - remainder; i++) {
                tabla.addCell(new Cell().setBorder(Border.NO_BORDER));
            }
        }

        document.add(tabla);
    }

    private void agregarFirmas(Document document, Formulario formulario, byte[] firmaClienteBytes) {
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        tabla.setWidth(UnitValue.createPercentValue(100));
        tabla.setMarginTop(20);

        SolidBorder borde = new SolidBorder(GRIS_BORDE, 0.8f);

        // Tarjeta FIRMA DEL CLIENTE
        Cell celdaCliente = new Cell().setBorder(borde).setPadding(12);
        celdaCliente.add(new Paragraph("FIRMA DEL CLIENTE")
                .setBold().setFontSize(9).setFontColor(GRIS_OSCURO).setMarginBottom(8));

        if (firmaClienteBytes != null && firmaClienteBytes.length > 0) {
            try {
                Image firma = new Image(ImageDataFactory.create(firmaClienteBytes));
                firma.setWidth(150).setHeight(60);
                celdaCliente.add(firma);
            } catch (Exception e) {
                celdaCliente.add(new Paragraph("\n_____________________________").setFontSize(10));
            }
        } else {
            celdaCliente.add(new Paragraph("\n_____________________________").setFontSize(10));
        }
        celdaCliente.add(new Paragraph("Nombre: " + valorVacio(formulario.getNombre_recibe()))
                .setFontSize(9).setMarginTop(6));
        celdaCliente.add(new Paragraph("Cédula: " + valorVacio(formulario.getCedula_recibe()))
                .setFontSize(9));
        tabla.addCell(celdaCliente);

        // Tarjeta FIRMA DEL TÉCNICO
        Cell celdaTecnico = new Cell().setBorder(borde).setPadding(12);
        celdaTecnico.add(new Paragraph("INFORMACIÓN DEL TECNICO")
                .setBold().setFontSize(9).setFontColor(GRIS_OSCURO).setMarginBottom(8));
        //celdaTecnico.add(new Paragraph("\n_____________________________").setFontSize(10));
        celdaTecnico.add(new Paragraph("Nombre: " + valorVacio(formulario.getNombre_tecnico()))
                .setFontSize(9).setMarginTop(6));
        celdaTecnico.add(new Paragraph("Celular: " + valorVacio(formulario.getTelefono_tecnico()))
                .setFontSize(9));
        tabla.addCell(celdaTecnico);

        document.add(tabla);

        agregarPie(document);
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

    // ── GENERACIÓN EN MEMORIA (activo para Railway) ───────────────────────────
    public byte[] generarPDF(Formulario formulario, List<byte[]> imagenesBytes, byte[] firmaClienteBytes) throws IOException {
        // ── MODO DISCO (desactivado para Railway) ──────────────────────────────
        // String nombreArchivo = "informe_" + formulario.getId() + ".pdf";
        // String rutaCompleta = pdfPath + nombreArchivo;
        // Files.createDirectories(Paths.get(pdfPath));
        // PdfWriter writer = new PdfWriter(rutaCompleta);
        // ── FIN MODO DISCO ─────────────────────────────────────────────────────

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);

        // Registrar banner superior (negro + diagonal amarilla) en cada página
        pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, new BannerSuperior());

        Document document = new Document(pdfDoc, PageSize.A4);
        // Margen superior = 28pt: 14pt del banner + 14pt de espacio blanco visible
        document.setMargins(28, 30, 20, 30);

        agregarEncabezado(document, formulario);
        agregarDatos(document, formulario);
        agregarDescripcion(document, formulario);

        if (imagenesBytes != null && !imagenesBytes.isEmpty()) {
            agregarImagenes(document, formulario, imagenesBytes);
        }

        agregarFirmas(document, formulario, firmaClienteBytes);

        document.close();

        // ── MODO DISCO (desactivado para Railway) ──────────────────────────────
        // return nombreArchivo;
        // ── FIN MODO DISCO ─────────────────────────────────────────────────────
        return baos.toByteArray();
    }
    // ── FIN GENERACIÓN EN MEMORIA ─────────────────────────────────────────────
}
