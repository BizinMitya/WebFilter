<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Результаты классификации</title>
    <style>
        .table {
            background: #f5ffff;
            border-collapse: collapse;
            text-align: center;
            font: 20pt sans-serif;
        }

        .table th {
            border-top: 1px solid #777777;
            border-bottom: 1px solid #777777;
            box-shadow: inset 0 1px 0 #999999, inset 0 -1px 0 #999999;
            background: #08d;
            color: white;
            padding: 10px 15px;
            position: relative;
        }

        .tr:nth-child(odd) {
            background: #ebf3f9;
        }

        .table td {
            border: 1px solid #e3eef7;
            padding: 10px 15px;
            position: relative;
            transition: all 0.5s ease;
        }

        .mark {
            background: #ff8080;
        }
    </style>
    <script>
        function markMax() {
            let probabilities = [];
            let counter = 1;
            let td;
            while ((td = document.getElementById('td' + counter)) !== null) {
                probabilities.push({
                    i: counter,
                    td: Number.parseFloat(td.innerHTML)
                });
                counter++;
            }
            let max = probabilities[0];
            for (let i = 0; i < probabilities.length; i++) {
                let obj = probabilities[i];
                if (obj.td > max.td) {
                    max = obj;
                }
            }
            let maxTr = document.getElementById('tr' + max.i);
            maxTr.className = 'mark';
        }
    </script>
</head>
<body onload="markMax()">
<table align="center" class="table">
    <thead>
    <tr>
        <th>#</th>
        <th>Класс</th>
        <th>Вероятность попадания</th>
    </tr>
    </thead>
    <tbody>
    <#list probabilities as probability>
        <tr class="tr" id="tr${probability?counter}">
            <td>${probability?counter}</td>
            <td>${probability.category}</td>
            <td id="td${probability?counter}">${probability.probability?string["0.##"]}</td>
        </tr>
    </#list>
    </tbody>
</table>
</body>
</html>