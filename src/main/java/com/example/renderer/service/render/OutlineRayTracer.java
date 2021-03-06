package com.example.renderer.service.render;

import com.example.renderer.model.Scene;
import com.example.renderer.model.object.Renderable;
import javafx.concurrent.Task;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.renderer.service.render.RayTraceUtils.getDirectionForPixel;
import static com.example.renderer.service.render.RayTraceUtils.getExecutor;

@Component
public class OutlineRayTracer implements TaskAwareRenderer {

    @Value("${selectionColor}")
    private Color selection;

    @Override
    public Image render(final Scene scene, Task task) throws InterruptedException {
        ExecutorService executor = getExecutor();
        WritableImage image = new WritableImage(scene.getWidth(), scene.getHeight());
        for (int j = 0; j < scene.getHeight(); j++) {
            for (int i = 0; i < scene.getWidth(); i++) {
                final int x = i;
                final int y = j;
                executor.execute(() -> {
                    if (objectHit(scene, x, y) && checkEdge(scene, x, y)) {
                        image.getPixelWriter().setColor(x, y, selection);
                    }
                });
            }
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        return image;
    }

    private boolean objectHit(Scene scene, int x, int y) {
        double fov = scene.getFovRadians();
        Point3D direction = getDirectionForPixel(x, y, scene.getWidth(), scene.getHeight(), fov);
        return scene.getSelected() instanceof Renderable
                && ((Renderable) scene.getSelected()).intersection(scene.getCameraOrigin(), direction).isHit();
    }

    private boolean checkEdge(Scene scene, int x, int y) {
        for (int k = -1; k <= 1; k++) {
            for (int l = -1; l <= 1; l++) {
                if (k != 0 && l != 0 && !objectHit(scene, x + l, y + k)) {
                    return true;
                }
            }
        }
        return false;
    }
}
