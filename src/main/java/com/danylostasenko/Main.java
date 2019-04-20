package com.danylostasenko;

import org.tensorflow.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class Main {
    private static final String MODEL_FILE_NAME = "basic14/learning/frozen_graph_def.pb";

    private static final Integer INPUT_SIZE = 10;
    private static final Float INPUT_MIN_VALUE = -5f;
    private static final Float INPUT_MAX_VALUE = 5f;

    public static void main(String[] args) throws Exception {
        Path modelPath = Paths.get(new File(MODEL_FILE_NAME).toURI());
        byte[] graph = Files.readAllBytes(modelPath);

        try (Graph g = new Graph(); Session sess = new Session(g)) {
            g.importGraphDef(graph);

            float[] input = new float[]{
                    -1.9f, 4.6f,
                    -1.8f, 4.5f,
                    -1.7f, 4.3f,
                    -1.6f, 4.3f,
                    -1.5f, 4.2f};
            Tensor observations = Tensor.create(input, Float.class);

            float[][] mask = new float[4][2];   // I think error is here. Means 4 branches, with size 2 each.
            Tensor actionMask = Tensor.create(mask, Float.class);
            Tensor<?> tensor = sess.runner()
                    .feed("vector_observation", observations)
                    .feed("action_masks", actionMask)
                    .fetch("action")
                    .run()  // Exception in thread "main" java.lang.IllegalArgumentException: In[0] is not a matrix
                    .get(0);

            System.out.println("Result: " + tensor.intValue());
        }
    }

    private static void printInputValues(float[] values){
        String inputValues = "";
        for (int i = 0; i < values.length; i++) {
            inputValues += values[i] + " ";
        }
        System.out.println("Input values: " + inputValues);
    }

    private static float getRandomFloat(float min, float max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random random = new Random();
        return min + (max - min) * random.nextFloat();
    }

    private static float[] generateRandomData(int inputSize, float min, float max){
        float[] inputData = new float[inputSize];
        for (int i = 0; i < inputData.length; i++) {
            inputData[i] = (getRandomFloat(min, max));
        }
        printInputValues(inputData);
        return inputData;
    }

    private static void printOperationsFromGraph(Graph graph){
        graph.operations().forEachRemaining(o -> {
            if (o.toString().contains("vector_observation")
                    || o.toString().contains("action_masks")
                    || o.toString().contains("action")){
                System.out.println(o);   //  <Placeholder 'vector_observation'>,     <Placeholder 'action_masks'>,   <Identity 'action'>
            }
        });
    }
}