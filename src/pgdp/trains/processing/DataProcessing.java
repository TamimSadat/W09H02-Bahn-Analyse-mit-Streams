package pgdp.trains.processing;

import pgdp.trains.connections.Station;
import pgdp.trains.connections.TrainConnection;
import pgdp.trains.connections.TrainStop;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataProcessing {

    public static Stream<TrainConnection> cleanDataset(Stream<TrainConnection> connections) {
        // TODO Task 1.
        if (connections == null) {
            return null;
        }
        else {
            List<TrainConnection> connectionsList = new ArrayList<>(connections.distinct().toList());
            connectionsList.sort(Comparator.comparing(connection -> connection.getFirstStop().scheduled()));
            Stream<TrainConnection> connectionsCancelled = connectionsList.stream()
                    .map(connection ->
                            connection.withUpdatedStops(connection.stops().stream()
                                    .filter(connectionsStop -> connectionsStop.kind() != TrainStop.Kind.CANCELLED)
                                    .toList()
                            )
                    );
            return connectionsCancelled;
        }
    }

    public static TrainConnection worstDelayedTrain(Stream<TrainConnection> connections) {
        // TODO Task 2.
        if (connections == null) {
          return null;
        }
        else {
            Comparator<TrainConnection> maxValueEachConnection = Comparator.comparingDouble(trainConnection ->
                    trainConnection.stops().stream().mapToDouble(delays -> delays.getDelay()).max().orElse(0)
            );
            TrainConnection worstDelay = connections.max(maxValueEachConnection).orElse(null);
            return worstDelay;
        }
        //https://stackoverflow.com/questions/31378324/how-to-find-maximum-value-from-a-integer-using-stream-in-java-8
        //Obiger Link, erste Antwort. Statt Intstream habe ich Doublestream verwendet.
        // Code nochmal abgeändert, da ich nicht max von allen max delays berücksichtigt habe.
    }

    public static double percentOfKindStops(Stream<TrainConnection> connections, TrainStop.Kind kind) {
        // TODO Task 3.
        if (connections == null || kind != TrainStop.Kind.CANCELLED && kind != TrainStop.Kind.ADDITIONAL && kind != TrainStop.Kind.REGULAR) {
            return 0;
        }
        else {
            ArrayList<TrainConnection> trainConnections = new ArrayList<>(connections.toList());
            double nmberOfallStops = trainConnections.stream()
                    .map(TrainConnection::stops)
                    .mapToLong(List::size).sum();
            if (nmberOfallStops == 0) {
                return 0;
            }
            else {
                double allKind = trainConnections.stream()
                        .map(TrainConnection::stops)
                        .flatMap(List::stream).filter(trainStop -> trainStop.kind() == kind)
                        .count();
                return (allKind / nmberOfallStops) * 100;
            }
            //https://stackoverflow.com/questions/25147094/how-can-i-turn-a-list-of-lists-into-a-list-in-java-8
            //Für flatmap(List::stream) die Quelle dazu -> Erste Antwort mit meisten Upvotes
        }
    }

    public static double averageDelayAt(Stream<TrainConnection> connections, Station station) {
        // TODO Task 4.
        List<TrainStop> stops = connections
                .flatMap(trainConnection -> trainConnection.stops().stream())
                .filter(stop -> stop.station().equals(station)).toList();
        if (stops.isEmpty()) {
            return 0;
        }
        else {
            return (double) stops.stream().mapToLong(TrainStop::getDelay).sum() / stops.size();
        }
    }

    public static Map<String, Double> delayComparedToTotalTravelTimeByTransport(Stream<TrainConnection> connections) {
        // TODO Task 5.
        return null;
    }

    public static Map<Integer, Double> averageDelayByHour(Stream<TrainConnection> connections) {
        // TODO Task 6.
        return null;
    }

    public static void main(String[] args) {
        // Um alle Verbindungen aus einer Datei zu lesen, verwendet DataAccess.loadFile("path/to/file.json"), etwa:
        // Stream<TrainConnection> trainConnections = DataAccess.loadFile("connections_test/fullDay.json");

        // Oder alternativ über die API, dies aber bitte sparsam verwenden, um die API nicht zu überlasten.
        //Stream<TrainConnection> trainsMunich = DataAccess.getDepartureBoardNowFor(Station.MUENCHEN_HBF);

        List<TrainConnection> trainConnections = List.of(
                new TrainConnection("ICE 2", "ICE", "2", "DB", List.of(
                        new TrainStop(Station.MUENCHEN_HBF,
                                LocalDateTime.of(2022, 12, 1, 11, 0),
                                LocalDateTime.of(2022, 12, 1, 11, 0),
                                TrainStop.Kind.REGULAR),
                        new TrainStop(Station.NUERNBERG_HBF,
                                LocalDateTime.of(2022, 12, 1, 11, 30),
                                LocalDateTime.of(2022, 12, 1, 12, 0),
                                TrainStop.Kind.REGULAR)
                )),
                new TrainConnection("ICE 1", "ICE", "1", "DB", List.of(
                        new TrainStop(Station.MUENCHEN_HBF,
                                LocalDateTime.of(2022, 12, 1, 10, 0),
                                LocalDateTime.of(2022, 12, 1, 10, 0),
                                TrainStop.Kind.REGULAR),
                        new TrainStop(Station.NUERNBERG_HBF,
                                LocalDateTime.of(2022, 12, 1, 10, 30),
                                LocalDateTime.of(2022, 12, 1, 10, 30),
                                TrainStop.Kind.REGULAR)
                )),
                new TrainConnection("ICE 3", "ICE", "3", "DB", List.of(
                        new TrainStop(Station.MUENCHEN_HBF,
                                LocalDateTime.of(2022, 12, 1, 12, 0),
                                LocalDateTime.of(2022, 12, 1, 12, 0),
                                TrainStop.Kind.REGULAR),
                        new TrainStop(Station.AUGSBURG_HBF,
                                LocalDateTime.of(2022, 12, 1, 12, 20),
                                LocalDateTime.of(2022, 12, 1, 13, 0),
                                TrainStop.Kind.CANCELLED),
                        new TrainStop(Station.NUERNBERG_HBF,
                                LocalDateTime.of(2022, 12, 1, 13, 30),
                                LocalDateTime.of(2022, 12, 1, 13, 30),
                                TrainStop.Kind.REGULAR)
                ))
        );

        List<TrainConnection> cleanDataset = cleanDataset(trainConnections.stream()).toList();
        //System.out.println(cleanDataset);
        // cleanDataset sollte sortiert sein: [ICE 1, ICE 2, ICE 3] und bei ICE 3 sollte der Stopp in AUGSBURG_HBF
        // nicht mehr enthalten sein.

        TrainConnection worstDelayedTrain = worstDelayedTrain(trainConnections.stream());
        // worstDelayedTrain sollte ICE 3 sein. (Da der Stop in AUGSBURG_HBF mit 40 Minuten Verspätung am spätesten ist.)

        double percentOfKindStops = percentOfKindStops(trainConnections.stream(), TrainStop.Kind.ADDITIONAL);
        // percentOfKindStops REGULAR sollte 85.71428571428571 sein, CANCELLED 14.285714285714285.
        System.out.println(percentOfKindStops);

        double averageDelayAt = averageDelayAt(trainConnections.stream(), Station.NUERNBERG_HBF);

        // averageDelayAt sollte 10.0 sein. (Da dreimal angefahren und einmal 30 Minuten Verspätung).

        Map<String, Double> delayCompared = delayComparedToTotalTravelTimeByTransport(trainConnections.stream());
        // delayCompared sollte ein Map sein, die für ICE den Wert 16.666666666666668 hat.
        // Da ICE 2 0:30 geplant hatte, aber 1:00 gebraucht hat, ICE 1 0:30 geplant und gebraucht hatte, und
        // ICE 3 1:30 geplant und gebraucht hat. Zusammen also 2:30 geplant und 3:00 gebraucht, und damit
        // (3:00 - 2:30) / 3:00 = 16.666666666666668.

        Map<Integer, Double> averageDelayByHourOfDay = averageDelayByHour(trainConnections.stream());
        // averageDelayByHourOfDay sollte ein Map sein, die für 10, 11 den Wert 0.0 hat, für 12 den Wert 15.0 und
        // für 13 den Wert 20.0.

    }


}
