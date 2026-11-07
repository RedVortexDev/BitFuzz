// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core;

import red.vortx.bitfuzz.buffer.impl.CountingByteWriter;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.field.Field;
import red.vortx.bitfuzz.schema.api.field.type.CompositeType;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.util.*;

/// Shows which value each byte of an encoding belongs to.
/// ```
/// 05 73 74 65 76 65 00 00 00 0c 01
/// ╰╮ ╰──────┬─────╯ ╰────┬────╯ ╰╮
///  │        │            │       ╰─ online: true
///  │        │            ╰─ level: 12
///  │        ╰─ name: "steve"
///  ╰─ name.length: 5
/// ```
public enum WireDump {
    ;

    private static final HexFormat HEX = HexFormat.of();
    private static final int BYTES_PER_ROW = 16;
    private static final int CELL_WIDTH = 3;
    private static final String LABEL_PREFIX = "╰─ ";

    /// Encodes values and shows which value each byte belongs to.
    ///
    /// @param schema the schema to encode with
    /// @param values the values to encode
    /// @return the bytes with every value marked under them
    public static String explain(final Schema schema, final List<Object> values) {
        Objects.requireNonNull(schema, "schema");
        Objects.requireNonNull(values, "values");

        final byte[] bytes = Bytes.encode(schema, values);
        final List<Span> spans = new ArrayList<>();
        int offset = 0;
        for (int i = 0; i < schema.fields().size(); i++) {
            final Field field = schema.fields().get(i);
            for (final Segment segment : explainValue(field.type(), values.get(i), field.name())) {
                spans.add(new Span(offset, offset + segment.size(), segment.label()));
                offset += segment.size();
            }
        }

        if (offset != bytes.length) {
            throw new IllegalStateException("Explained %d bytes but the encoding has %d"
                    .formatted(offset, bytes.length)
            );
        }

        final StringJoiner rows = new StringJoiner("\n");
        for (int from = 0; from < bytes.length; from += BYTES_PER_ROW) {
            rows.add(renderRow(bytes, from, Math.min(from + BYTES_PER_ROW, bytes.length), spans));
        }

        return rows.toString();
    }

    private static List<Segment> explainValue(final Type<?> type, final Object value, final String path) {
        if (!(type instanceof final CompositeType<?> composite)) {
            return List.of(new Segment(sizeOf(type, value), "%s: %s".formatted(path, describe(value))));
        }

        final List<Segment> segments = new ArrayList<>();
        for (final CompositeType.Part part : getParts(composite, value)) {
            segments.addAll(explainValue(part.type(), part.value(), joinPath(path, part.label())));
        }

        final int partsSize = segments.stream().mapToInt(Segment::size).sum();
        final int size = sizeOf(type, value);
        if (partsSize != size) {
            throw new IllegalStateException("The parts of %s add up to %d bytes but it writes %d"
                    .formatted(type.getClass().getSimpleName(), partsSize, size)
            );
        }

        return segments;
    }

    private static List<CompositeType.Part> getParts(final CompositeType<?> type, final Object value) {
        //noinspection unchecked
        return ((CompositeType<Object>) type).parts(value);
    }

    private static String joinPath(final String path, final String label) {
        if (label.isEmpty() || label.startsWith("[")) {
            return path + label;
        }

        return "%s.%s".formatted(path, label);
    }

    private static int sizeOf(final Type<?> type, final Object value) {
        final CountingByteWriter counter = new CountingByteWriter();
        type.writeUnchecked(counter, value);
        return counter.count();
    }

    private static String describe(final Object value) {
        if (value instanceof final String string) {
            return "\"%s\"".formatted(string);
        }

        return String.valueOf(value);
    }

    private static String renderRow(final byte[] bytes, final int from, final int to, final List<Span> spans) {
        final StringJoiner hex = new StringJoiner(" ");
        for (int i = from; i < to; i++) {
            hex.add(HEX.toHexDigits(bytes[i]));
        }

        final StringBuilder brackets = new StringBuilder();
        final List<Label> labels = new ArrayList<>();
        for (final Span span : spans) {
            final int start = Math.max(span.start(), from);
            final int end = Math.min(span.end(), to);
            if (start >= end) {
                continue;
            }

            final int column = (start - from) * CELL_WIDTH;
            final boolean opens = span.start() >= from;
            pad(brackets, column);
            brackets.append(drawBracket(end - start, opens, span.end() <= to));

            if (opens) {
                labels.add(new Label(column + getTickOffset(end - start), span.label()));
            }
        }

        final StringBuilder row = new StringBuilder()
                .append(hex).append('\n')
                .append(brackets).append('\n');
        for (final String line : layoutLabels(labels)) {
            row.append(line).append('\n');
        }

        return row.toString();
    }

    private static String drawBracket(final int size, final boolean opens, final boolean closes) {
        final char[] bracket = new char[size * CELL_WIDTH - 1];
        final int last = bracket.length - 1;
        final int tick = getTickOffset(size);
        Arrays.fill(bracket, '─');

        if (closes) {
            bracket[last] = '╯';
        }
        if (opens) {
            final boolean tickIsCorner = closes && tick == last;
            bracket[0] = '╰';
            bracket[tick] = tickIsCorner ? '╮' : '┬';
        }

        return new String(bracket);
    }

    private static int getTickOffset(final int size) {
        return (size * CELL_WIDTH - 1) / 2;
    }

    /// Puts each label on the first line where it doesn't run into the next one. The rest continue down with `│`.
    private static List<String> layoutLabels(final List<Label> labels) {
        final List<String> lines = new ArrayList<>();
        List<Label> pending = labels;
        while (!pending.isEmpty()) {
            final StringBuilder line = new StringBuilder();
            final List<Label> next = new ArrayList<>();
            for (int i = 0; i < pending.size(); i++) {
                final Label label = pending.get(i);
                final boolean isLast = i == pending.size() - 1;
                final boolean fits = isLast || label.end() < pending.get(i + 1).column();
                pad(line, label.column());

                if (fits) {
                    line.append(LABEL_PREFIX).append(label.text());
                } else {
                    line.append('│');
                    next.add(label);
                }
            }

            lines.add(line.toString());
            pending = next;
        }

        return lines;
    }

    private static void pad(final StringBuilder builder, final int column) {
        builder.repeat(' ', column - builder.length());
    }

    private record Segment(int size, String label) {
    }

    private record Span(int start, int end, String label) {
    }

    private record Label(int column, String text) {

        private int end() {
            return this.column + LABEL_PREFIX.length() + this.text.length();
        }

    }

}
