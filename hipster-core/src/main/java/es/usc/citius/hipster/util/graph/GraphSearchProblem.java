/*
 * Copyright 2014 CITIUS <http://citius.usc.es>, University of Santiago de Compostela.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package es.usc.citius.hipster.util.graph;


import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.function.CostFunction;
import es.usc.citius.hipster.model.function.TransitionFunction;
import es.usc.citius.hipster.model.problem.InformedSearchProblem;
import es.usc.citius.hipster.model.problem.ProblemBuilder;

/**
 * @author Pablo Rodríguez Mier
 */
public final class GraphSearchProblem {

    public static class FromVertex<V> {
        private V fromVertex;

        public FromVertex(V fromVertex) {
            this.fromVertex = fromVertex;
        }

        public class ToVertex {
            private V toVertex;

            public ToVertex(V toVertex) {
                this.toVertex = toVertex;
            }

            /*
            public <E extends Comparable<E>> InformedSearchProblem<E, V, E> ing(final HipsterGraph<V,E> g){

            }*/

            public <E> InformedSearchProblem<E, V, Double> in(final HipsterGraph<V, E> graph) {
                TransitionFunction<E,V> tf;
                if (graph instanceof HipsterDirectedGraph){
                    final HipsterDirectedGraph<V,E> dg = (HipsterDirectedGraph<V,E>) graph;
                    tf = new TransitionFunction<E, V>() {
                        @Override
                        public Iterable<Transition<E, V>> transitionsFrom(final V state) {
                            return Iterables.transform(dg.outgoingEdgesOf(state), new Function<GraphEdge<V, E>, Transition<E,V>>() {
                                @Override
                                public Transition<E,V> apply(GraphEdge<V, E> edge) {
                                    return Transition.create(state, edge.getEdgeValue(), edge.getVertex2());
                                }
                            });
                        }
                    };
                } else {
                    tf = new TransitionFunction<E, V>() {
                        @Override
                        public Iterable<Transition<E, V>> transitionsFrom(final V state) {
                            return Iterables.transform(graph.edgesOf(state), new Function<GraphEdge<V, E>, Transition<E,V>>() {
                                @Override
                                public Transition<E,V> apply(GraphEdge<V, E> edge) {
                                    V oppositeVertex = edge.getVertex1().equals(state) ? edge.getVertex2() : edge.getVertex1();
                                    return Transition.create(state, edge.getEdgeValue(), oppositeVertex);
                                }
                            });
                        }
                    };
                }
                return ProblemBuilder.create()
                        .initialState(fromVertex)
                        .goalState(toVertex)
                        .defineProblemWithExplicitActions()
                        .useTransitionFunction(tf)
                        .useCostFunction(new CostFunction<E, V, Double>() {
                            @Override
                            public Double evaluate(Transition<E, V> transition) {
                                return (Double)transition.getAction();
                            }
                        })
                        .build();
            }

        }
        public ToVertex to(V vertex) {
            return new ToVertex(vertex);
        }
    }
    public static <V> FromVertex<V> from(V vertex) {
        return new FromVertex<V>(vertex);
    }
}
