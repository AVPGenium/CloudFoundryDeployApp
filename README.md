# CloudFoundry Deploy App
Java-клиент для деплоя приложений в Cloud Foundry. Использует Reactive Streams для выполнения операций с CloudFoundry.

### Шпаргалка по Reactive Streams:
Продюсеры (`Producer`): `Mono<T>` - создает один продукт, `Flux<T>` - создает от 0 до бесконечности продуктов.

Реактивные стримы обладают методами, похожими на методы обычных стримов:

`fromIterable`

`flatMap`

`then`

Взаимодействие с CF происходит посредством выполнения Producer-ами запросов к сервису CF
(т.е. асинхронно, дается только обещание, что запрос будет выполнен).