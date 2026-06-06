package com.starweave.service.impl;

import com.starweave.dto.CaptchaResponse;
import com.starweave.service.CaptchaService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    private static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 4;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 44;
    private static final Duration TTL = Duration.ofMinutes(5);

    private final SecureRandom random = new SecureRandom();
    private final Map<String, CaptchaEntry> captchas = new ConcurrentHashMap<>();

    @Override
    public CaptchaResponse generate() {
        cleanupExpired();

        String captchaId = UUID.randomUUID().toString();
        String code = randomCode();
        captchas.put(captchaId, new CaptchaEntry(code, Instant.now().plus(TTL)));

        return new CaptchaResponse(captchaId, renderBase64Png(code), TTL.toSeconds());
    }

    @Override
    public boolean verify(String captchaId, String captchaCode) {
        if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            return false;
        }

        CaptchaEntry entry = captchas.remove(captchaId);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            return false;
        }

        return entry.code().equalsIgnoreCase(captchaCode.strip());
    }

    private String randomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return code.toString();
    }

    private String renderBase64Png(String code) {
        try {
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(248, 250, 252));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            for (int i = 0; i < 18; i++) {
                g.setColor(randomMutedColor(150, 230));
                g.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                        2 + random.nextInt(4), 2 + random.nextInt(4));
            }

            g.setStroke(new BasicStroke(1.2f));
            for (int i = 0; i < 5; i++) {
                g.setColor(randomMutedColor(110, 190));
                g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                        random.nextInt(WIDTH), random.nextInt(HEIGHT));
            }

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
            FontMetrics metrics = g.getFontMetrics();
            int charWidth = WIDTH / (CODE_LENGTH + 1);
            int baseline = (HEIGHT - metrics.getHeight()) / 2 + metrics.getAscent();

            for (int i = 0; i < code.length(); i++) {
                g.setColor(randomTextColor());
                int x = 12 + i * charWidth + random.nextInt(5);
                int y = baseline + random.nextInt(7) - 3;
                double angle = Math.toRadians(random.nextInt(25) - 12);
                g.rotate(angle, x, y);
                g.drawString(String.valueOf(code.charAt(i)), x, y);
                g.rotate(-angle, x, y);
            }

            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Captcha image generation failed", e);
        }
    }

    private Color randomMutedColor(int min, int max) {
        int range = max - min + 1;
        return new Color(min + random.nextInt(range), min + random.nextInt(range), min + random.nextInt(range));
    }

    private Color randomTextColor() {
        return new Color(30 + random.nextInt(70), 45 + random.nextInt(80), 70 + random.nextInt(90));
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, CaptchaEntry>> iterator = captchas.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().expiresAt().isBefore(now)) {
                iterator.remove();
            }
        }
    }

    private record CaptchaEntry(String code, Instant expiresAt) {}
}
