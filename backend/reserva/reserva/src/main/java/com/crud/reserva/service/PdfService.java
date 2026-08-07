package com.crud.reserva.service;

import com.crud.reserva.model.Reserva;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfService {

    private static final String NOME_SISTEMA = "Sistema de Reserva de Salas";
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter EMISSAO_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");

    public byte[] gerarMapaDiarioReservas(List<Reserva> reservas, LocalDate dataConsulta) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 36, 36, 42, 42);
            PdfWriter.getInstance(document, outputStream);

            document.open();
            adicionarCabecalho(document, dataConsulta);
            adicionarReservasPorAmbiente(document, reservas);
            adicionarResumo(document, reservas.size());
            adicionarRodape(document);
            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Erro ao gerar PDF do mapa diario de reservas", ex);
        }

        return outputStream.toByteArray();
    }

    private void adicionarCabecalho(Document document, LocalDate dataConsulta) throws DocumentException {
        Font sistemaFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Font textoFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

        Paragraph sistema = new Paragraph(NOME_SISTEMA, sistemaFont);
        sistema.setAlignment(Element.ALIGN_CENTER);
        document.add(sistema);

        Paragraph titulo = new Paragraph("MAPA DIARIO DE RESERVAS", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(8);
        titulo.setSpacingAfter(18);
        document.add(titulo);

        document.add(new Paragraph("Data consultada: " + dataConsulta.format(DATA_BR), textoFont));
        document.add(new Paragraph("Relatorio emitido em: " + LocalDateTime.now().format(EMISSAO_BR), textoFont));
        document.add(new Paragraph(" "));
    }

    private void adicionarReservasPorAmbiente(Document document, List<Reserva> reservas) throws DocumentException {
        Map<String, List<Reserva>> reservasPorAmbiente = reservas.stream()
                .collect(Collectors.groupingBy(
                        reserva -> reserva.getAmbiente() != null ? reserva.getAmbiente().getNome() : "Ambiente nao informado",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Font ambienteFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK);

        for (Map.Entry<String, List<Reserva>> grupo : reservasPorAmbiente.entrySet()) {
            Paragraph ambiente = new Paragraph(grupo.getKey(), ambienteFont);
            ambiente.setSpacingBefore(10);
            ambiente.setSpacingAfter(6);
            document.add(ambiente);

            document.add(criarTabelaReservas(grupo.getValue()));
        }
    }

    private PdfPTable criarTabelaReservas(List<Reserva> reservas) {
        PdfPTable table = new PdfPTable(new float[]{1.3f, 3.8f, 1.8f});
        table.setWidthPercentage(100);

        adicionarCelulaCabecalho(table, "Horario");
        adicionarCelulaCabecalho(table, "Professor responsavel");
        adicionarCelulaCabecalho(table, "Status");

        for (Reserva reserva : reservas) {
            adicionarCelula(table, valorOuPadrao(reserva.getHorario()));
            adicionarCelula(table, valorOuPadrao(reserva.getProfessor()));
            adicionarCelula(table, valorOuPadrao(reserva.getStatus()));
        }

        return table;
    }

    private void adicionarResumo(Document document, int totalReservas) throws DocumentException {
        Font resumoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);

        Paragraph total = new Paragraph("Total de Reservas: " + totalReservas, resumoFont);
        total.setSpacingBefore(16);
        document.add(total);
    }

    private void adicionarRodape(Document document) throws DocumentException {
        Font rodapeFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.DARK_GRAY);

        Paragraph rodape = new Paragraph("Documento institucional gerado pelo Sistema de Reserva de Salas.", rodapeFont);
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setSpacingBefore(28);
        document.add(rodape);
    }

    private void adicionarCelulaCabecalho(PdfPTable table, String texto) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(new Color(55, 65, 81));
        cell.setPadding(7);
        table.addCell(cell);
    }

    private void adicionarCelula(PdfPTable table, String texto) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setPadding(7);
        table.addCell(cell);
    }

    private String valorOuPadrao(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }
}
