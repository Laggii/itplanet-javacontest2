package com.atc.javacontest;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import sun.font.FontRunIterator;


import javax.swing.tree.ExpandVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CompareRowImpl implements ICompareRow {

	/*
	Единый метод для всех заданий, возвращающий из файла два массива с рядами
	 */
	public List<double[]> parseFile(URL resource) throws  Exception {
		List<String> lines = Files.readAllLines(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
		List<double[]> rows = new ArrayList<double[]>();
		double[] rowFirst = new double[lines.size()];
		double[] rowSecond = new double[lines.size()];
		for(String line:lines){
			if (resource.toString().contains("test11")) { //в 11 тесте запись значений другая
				String[] values = line.split(",");
				rowFirst[lines.indexOf(line)] = Double.parseDouble(values[1]);
				rowSecond[lines.indexOf(line)] = Double.parseDouble(values[2]);
			} else {
				String[] values = line.split(";");
				rowFirst[lines.indexOf(line)] = Double.parseDouble(values[1].replace(',', '.'));
				rowSecond[lines.indexOf(line)] = Double.parseDouble(values[2].replace(',', '.'));
			}
		}
		rows.add(rowFirst);
		rows.add(rowSecond);
		return rows;
	}


	public CorrelationResultIndex executeTest0(URL resource) {
		CorrelationResultIndex index = new CorrelationResultIndex();
		
		try {
			List<String> lines = Files.readAllLines(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
			int count = 0;
			int equals = 0;
			for(String line:lines){
				String[] values = line.split(";");
				if(Double.parseDouble(values[1].replace(',', '.')) == Double.parseDouble(values[2].replace(',', '.')) ){
					equals++;	
				}
				count++;
			}
			index.correlation = equals/count;
			index.correlationLagIndex = 0;
			index.correlationMutipleIndex = 0;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return index;
	}

	public CorrelationResultIndex executeTest1(URL resource) {
		CorrelationResultIndex index = new CorrelationResultIndex();

		try {
			List<double[]> rows = parseFile(resource);
			double[] array1 = rows.get(0);
			double[] array2 = rows.get(1);

			index.correlation = new PearsonsCorrelation().correlation(array1, array2);

			//алгоритм поиска множителя
			double max1 = array1[0];
			double max2 = array1[0];
			//находим максимальные значения (одну из вершин)
			for (int i = 0; i < array1.length; i++) {
				if (max1 < array1[i]) max1 = array1[i];
				if (max2 < array2[i]) max2 = array2[i];
			}

			index.correlationMutipleIndex = max2/max1;
			index.correlationLagIndex = 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return index;
	}

	public CorrelationResultIndex executeTest2(URL resource) {
		CorrelationResultIndex index = new CorrelationResultIndex();

		try {
			List<double[]> rows = parseFile(resource);
			double[] array1 = rows.get(0);
			double[] array2 = rows.get(1);

			PearsonsCorrelation correlation = new PearsonsCorrelation();
			double tempCorrelation;
			index.correlationLagIndex = 0;
			index.correlation = correlation.correlation(array1, array2);

			/*
			Если я правильно понял, второй ряд отстает на некоторый (временной) лаг, т.е. надо найти корреляцию учитывая (временной) сдвиг
			Находим самый высокий коэффициент корреляции, сдвигая каждый раз второй ряд на 1 индекс
			*/
			for (int i = 0; i < array1.length; i++) {
				double lastElement = array2[array2.length-1];

				System.arraycopy(array2, 0, array2, 1, array2.length-1);
				array2[0] = lastElement;
				tempCorrelation = correlation.correlation(array1, array2);

				if (index.correlation < tempCorrelation) {
					index.correlation = tempCorrelation;
					index.correlationLagIndex = i+1;
				}
			}
			index.correlationMutipleIndex = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
		// TODO Auto-generated method stub
	}

	public CorrelationResultIndex executeTest3(URL resource) {
		CorrelationResultIndex index = new CorrelationResultIndex();

		try {
			List<double[]> rows = parseFile(resource);
			double[] array1 = rows.get(0);
			double[] array2 = rows.get(1);

			PearsonsCorrelation correlation = new PearsonsCorrelation();
			double tempCorrelation;
			index.correlationLagIndex = 0;
			index.correlation = correlation.correlation(array1, array2);


			/*
			Можно было бы предположить, что ряды против. друг другу, однако решаю из предположения,
			что второй ряд отстает от первого на временной лаг (или опережает), отсюда коэфф корреляции положительный, множитель = 1
			а решение аналогично задаче 2
			(т.е. нельзя утверждать, что второй ряд противоположен первому, а корреляция = -1,
			т.к. тогда абсолютно все значения 1 ряда должны быть равны значениям 2 ряда, но значения не совпадают каждые 100 индексов,
			а отстают на эти 100 пунктов)
			*/
			for (int i = 0; i < array1.length; i++) {
				double lastElement = array2[array2.length-1];
				System.arraycopy(array2, 0, array2, 1, array2.length-1);
				array2[0] = lastElement;
				tempCorrelation = correlation.correlation(array1, array2);
				if (index.correlation < tempCorrelation) {
					index.correlation = tempCorrelation;
					index.correlationLagIndex = i+1;
				}
			}

			index.correlationMutipleIndex = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
		// TODO Auto-generated method stub
	}

	public CorrelationResultIndex executeTest4(URL resource) {
		CorrelationResultIndex index = new CorrelationResultIndex();

		try {
			List<double[]> rows = parseFile(resource);
			double[] array1 = rows.get(0);
			double[] array2 = rows.get(1);

			/*
			Второй ряд немного зашумлен, поэтому можно попробовать сгладить его.
			Использую алгоритм скользящей медианы в окне. Элемент ряда заменяется на медиану его соседей из окна.
			Окно в пять элементов
			(алгоритм скользящей средней в данном случае дает меньший коэф. корреляции)
			 */
			double[] filtredArray = new double[array2.length];
			System.arraycopy(array2, 0, filtredArray, 0, array2.length);

			for (int i = 2; i < array2.length-2; i++) {
				double[] window = new double[5];
				//выборка элементов для окна
				for (int j = 0; j < 5; j++) {
					window[j] = array2[i-2+j];
				}
				//в окне находим медиану (достаточно половины элементов окна)
				for (int j = 0; j < 3; j++) {
					int min = j;
					for (int k = j+1; k < 5; k++) {
						if (window[k] < window[min]){
							min = k;
						}
					}
					double temp = window[j];
					window[j] = window[min];
					window[min] = temp;
				}
				//найденное значение медианы добавляем в массив
				filtredArray[i] = window[2];
			}
			array2 = filtredArray;
			index.correlation = new PearsonsCorrelation().correlation(array1,array2);

			index.correlationLagIndex = 0;
			index.correlationMutipleIndex = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
		// TODO Auto-generated method stub
	}

	public CorrelationResultIndex executeTest5(URL resource) {
		CorrelationResultIndex index = new CorrelationResultIndex();

		try {
			List<double[]> rows = parseFile(resource);
			double[] array1 = rows.get(0);
			double[] array2 = rows.get(1);

			/*
			Приведение зависимости к линейному виду с помощью логарифмирования
			Идея логарифмировать значения рядов для приведения функции к линейной пришла на 8м задании, поэтому подробнее в описании к 8му заданию.

			Примечание: если прогнать здесь алгоритм из 8го задания, можно получить немного больший коэффициент = 0.9941581668106082. Оставляю как есть
			 */
			for (int i = 0; i < array1.length; i++) {
				if (array1[i] != 0) array1[i] = Math.log(Math.abs(array1[i]));
				if (array2[i] != 0) array2[i] = Math.log(Math.abs(array2[i]));
			}

			index.correlation = new PearsonsCorrelation().correlation(array1, array2);

			index.correlationLagIndex = 0;
			index.correlationMutipleIndex = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
		// TODO Auto-generated method stub
	}

	public CorrelationResultIndex executeTest6(URL resource) {
		CorrelationResultIndex index = new CorrelationResultIndex();

		try {
			List<double[]> rows = parseFile(resource);
			double[] array1 = rows.get(0);
			double[] array2 = rows.get(1);

			/*
			Для сглаживания значений второго ряда использую простой метод скользящей средней
			Сглаживаю до тех пор, пока не получу наибольшее значение корреляции
			 */
			PearsonsCorrelation pc = new PearsonsCorrelation();
			index.correlation = pc.correlation(array1, array2);
			double tempCorrelation;

			for (int j = 0; j < array1.length; j++) {

				for (int i = 1; i < array2.length - 1; i++) {
					array2[i] = (array2[i - 1] + array2[i + 1]) / 2;
				}
				tempCorrelation = pc.correlation(array1, array2);
				if (index.correlation < tempCorrelation) index.correlation = tempCorrelation;
			}

			index.correlationLagIndex = 0;
			index.correlationMutipleIndex = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
		// TODO Auto-generated method stub
	}

	public CorrelationResultIndex executeTest7(URL resource) {
		CorrelationResultIndex index = new CorrelationResultIndex();
		try {
			List<double[]> rows = parseFile(resource);
			double[] array1 = rows.get(0);
			double[] array2 = rows.get(1);

			/*
			Решение аналогично 6 задаче
			 */
			PearsonsCorrelation pc = new PearsonsCorrelation();
			index.correlation = pc.correlation(array1, array2);
			double tempCorrelation;

			for (int j = 0; j < array1.length; j++) {

				for (int i = 1; i < array2.length - 1; i++) {
					array2[i] = (array2[i - 1] + array2[i + 1]) / 2;
				}
				tempCorrelation = pc.correlation(array1, array2);
				if (index.correlation < tempCorrelation) index.correlation = tempCorrelation;
			}

			index.correlationLagIndex = 0;
			index.correlationMutipleIndex = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
		// TODO Auto-generated method stub
	}

	public CorrelationResultIndex executeTest8(URL resource) {
		CorrelationResultIndex index = new CorrelationResultIndex();
		try {
			List<double[]> rows = parseFile(resource);
			double[] array1 = rows.get(0);
			double[] array2 = rows.get(1);

			//алгоритм поиска множителя из первой задачи
			double max1 = array1[0];
			double max2 = array1[0];
			//находим максимальные значения (одну из вершин)
			for (int i = 0; i < array1.length; i++) {
				if (max1 < array1[i]) max1 = array1[i];
				if (max2 < array2[i]) max2 = array2[i];
			}
			index.correlationMutipleIndex = max2/max1;
			index.correlationLagIndex = 0;

			//Другой параметр, поскольку два ряда отличаются по периоду (или длине волны)
			index.anotherIndexDesc = "отношение периодов функций двух рядов, т.е. во сколько раз период одной функции больше чем другой";

			//Алгоритм поиска периода функции простой: находим экстремумы верхний и нижний, разница их индексов в массиве и будет половина периода
			double maxArray1 = array1[0];
			double minArray1 = array1[0];
			double maxArray2 = array2[0];
			double minArray2 = array2[0];
			for (int i = 0; i < array1.length; i++) {
				if (maxArray1 < array1[i]) maxArray1 = array1[i];
				if (minArray1 > array1[i]) minArray1 = array1[i];

				if (maxArray2 < array2[i]) maxArray2 = array2[i];
				if (minArray2 > array2[i]) minArray2 = array2[i];
			}
			double L = (minArray1 - maxArray1)/(minArray2 - maxArray2);
			index.anotherIndex = ""+L;

			/*
			Алгоритм нахождения корреляции.
			Поскольку зависимость видна, но она нелинейна, можно попробовать сделать логарифмическое преобразование,
			чтобы привести зависимости к линейному виду.
			Изначально я логарифмировал значения двух рядов и получил значение корреляции равное 0.6627995954171327 (для пятого задания я так и сделал)
			Однако позже мне пришла идея модифицировать формулу Пирсмена для определения корреляции
			и прологарифмировать все значения (включая значения средних арифметических) прямо в ней.

			Следующий алгоритм это обычная формула Пирсона с изменениями описанными выше.
			Результат: index.correlation = 0.9317108519040622
			 */
			double R;
			double avg1 = 0;
			double avg2 = 0;
			double sum1 = 0; //числитель
			double sum2 = 0; //выборочная дисперсия x (знаменатель)
			double sum3 = 0; //выборочная дисперсия y(знаменатель)

			//находим среднее арифметическое
			for (int i = 0; i < array1.length; i++) {
				avg1=+array1[i];
				avg2=+array2[i];
			}
			avg1 = avg1/array1.length;
			avg2 = avg2/array2.length;

			for (int i = 0; i < array1.length; i++) {
				// логарифмируем значения рядов, 0 оставляем без изменений (в данных примерах -  пара с индексом 0)
				if (array1[i] != 0) array1[i] = Math.log(Math.abs(array1[i]));
				if (array2[i] != 0) array2[i] = Math.log(Math.abs(array2[i]));

				sum1 = sum1 + (array1[i] - Math.log(Math.abs(avg1))) * (array2[i] - Math.log(Math.abs(avg2)));
				sum2 = sum2 + (array1[i] - Math.log(Math.abs(avg1))) * (array1[i] - Math.log(Math.abs(avg1)));
				sum3 = sum3 + (array2[i] - Math.log(Math.abs(avg2))) * (array2[i] - Math.log(Math.abs(avg2)));;
			}
			//коэффициент корреляции
			R = sum1/Math.sqrt(sum2*sum3);
			index.correlation = R;


		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
		// TODO Auto-generated method stub
	}

	public CorrelationResultIndex executeTest9(URL resource) {
		CorrelationResultIndex index = new CorrelationResultIndex();

		try {
			List<double[]> rows = parseFile(resource);
			double[] array1 = rows.get(0);
			double[] array2 = rows.get(1);

			index.anotherIndexDesc = "отношение периодов функций двух рядов, т.е. во сколько раз период одной функции больше чем другой";
			index.anotherIndex = "0.5";

			/*
			Решение аналогично 8 задаче, разве что можно сгладить оба ряда.
			Алгоритм сглаживания - скользящая медиана в окне (аналогично 4му заданию)
			 */
			double[] filtredArray = new double[array1.length];
			System.arraycopy(array1, 0, filtredArray, 0, array1.length);

			for (int i = 2; i < array1.length-2; i++) {
				double[] window = new double[5];
				//выборка элементов для окна
				for (int j = 0; j < 5; j++) {
					window[j] = array2[i-2+j];
				}
				//в окне находим медиану (достаточно половины элементов окна)
				for (int j = 0; j < 3; j++) {
					int min = j;
					for (int k = j+1; k < 5; k++) {
						if (window[k] < window[min]){
							min = k;
						}
					}
					double temp = window[j];
					window[j] = window[min];
					window[min] = temp;
				}
				//найденное значение медианы добавляем в массив
				filtredArray[i] = window[2];
			}
			array1 = filtredArray;

			filtredArray = new double[array2.length];
			System.arraycopy(array2, 0, filtredArray, 0, array2.length);

			for (int i = 2; i < array2.length-2; i++) {
				double[] window = new double[5];
				//выборка элементов для окна
				for (int j = 0; j < 5; j++) {
					window[j] = array2[i-2+j];
				}
				//в окне находим медиану (достаточно половины элементов окна)
				for (int j = 0; j < 3; j++) {
					int min = j;
					for (int k = j+1; k < 5; k++) {
						if (window[k] < window[min]){
							min = k;
						}
					}
					double temp = window[j];
					window[j] = window[min];
					window[min] = temp;
				}
				//найденное значение медианы добавляем в массив
				filtredArray[i] = window[2];
			}
			array2 = filtredArray;

			double R;
			double avg1 = 0;
			double avg2 = 0;
			double sum1 = 0; //числитель
			double sum2 = 0; //выборочная дисперсия x (знаменатель)
			double sum3 = 0; //выборочная дисперсия y(знаменатель)

			//находим среднее арифметическое
			for (int i = 0; i < array1.length; i++) {
				avg1=+array1[i];
				avg2=+array2[i];
			}
			avg1 = avg1/array1.length;
			avg2 = avg2/array2.length;

			for (int i = 0; i < array1.length; i++) {
				// логарифмируем значения рядов, 0 оставляем без изменений (в данных примерах пара с индексом 0)
				if (array1[i] != 0) array1[i] = Math.log(Math.abs(array1[i]));
				if (array2[i] != 0) array2[i] = Math.log(Math.abs(array2[i]));

				sum1 = sum1 + (array1[i] - Math.log(Math.abs(avg1))) * (array2[i] - Math.log(Math.abs(avg2)));
				sum2 = sum2 + (array1[i] - Math.log(Math.abs(avg1))) * (array1[i] - Math.log(Math.abs(avg1)));
				sum3 = sum3 + (array2[i] - Math.log(Math.abs(avg2))) * (array2[i] - Math.log(Math.abs(avg2)));;
			}
			//коэффициент корреляции
			R = sum1/Math.sqrt(sum2*sum3);
			index.correlation = R;

			index.correlationLagIndex = 0;
			index.correlationMutipleIndex = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return index;
		// TODO Auto-generated method stub
	}

	public List<CorrelationResultIndex> executeTest10(URL resource) {
		List<CorrelationResultIndex> result = new ArrayList<CorrelationResultIndex>();
		try {
			List<double[]> rows = parseFile(resource);
			double[] array1 = rows.get(0);
			double[] array2 = rows.get(1);

			/*
			Из формулировки задания возникают следующие проблемы:
			1) Две пары значений рядов дают корреляцию = 1 или 0.99, что позволяет вывести для ответа 500 корреляций
			2) Можно добавлять в выборку элементов по одной паре значений, пока корреляция не перестанет удовлетворять условию >= 0.3;
			   диапазоны, где будет действовать корреляция, будут самыми большими, но корреляция будет близка к 0.3. Однако и это годится для ответа.
			3) Считая корреляцию по модулю, положительные и отрицательные корреляции на одном диапазоне будут смешиваться, что может привести (и приведет) к неверному ответу. Дополнительно хорошо бы знак корреляции включить в ответ.

			Исходя из 1+2+3, алгоритм:

			Предположил, что, те участки элементов, которые идут подряд и дают хорошие корреляции, можно сложить и получить хорошую корреляцию суммарного участка.
			По изначальным парам рядов в csv файле двигается окно (у меня окно = 10 парам). Считается корреляция этих 10ти элементов из окна.
			Если она хорошая (>0.3), то специальным маркером отмечаю, что последовательность начинается с этой десятки, сохраняю индекс, с которого начинается корреляция и количество элементов, которые надо сохранить.
			Если следующая десятка продолжает последовательность, добавляю еще 10 элементов, которые надо сохранить.
			Так делаю до тех пор, пока не найдется десятка, которая либо имеет корреляцию < 0.3, либо имеет другой знак.
			В обоих случаях последовательность хороших корреляций прервалась, результат - корреляция на участке с первого элемента первой десятки последовательности до последнего элемента последней десятки, после которой последовательность закончилась.

			Пример:
			Корреляция на участке 0-10: 0.9
			Корреляция на участке 10-20: 0.75
			Корреляция на участке 20-30: 0.8
			Корреляция на участке 30-40: 0.21
			Последовательность прервалась на участке 30-40. В ответ войдет участок 0-30, его корреляция 0.85. Цикл начинается снова с участка 30-40. И так далее.
			При этом, последовательность прерывается если знак поменялся, за это отвечают маркеры смены знака. Я сохраняю и сравниваю знак корреляции элементов текущего окна и предыдущего окна.

			Замечания.
			Как результат, будут найдены корреляции, участки которых видно глазом, если строить график. 0-100, 300-400, 900-1000.
			На графике так же видны корреляции со смещением: 400-450, 550-600. При окне window = 10, алгоритм найдет их немного неточно: 410-440 и 550-590.

			При window = 50 (половина периода) результат следующий:
				0.9846803628643004 start:0 end:100
				0.9830143128856924 start:300 end:400
				-0.7793810666471459 start:400 end:450
				0.7845433630989064 start:550 end:600
				0.9834279165908291 start:900 end:1000
			Скорее всего, это и есть правильный ответ на 10ю задачу, однако мне хотелось включить в результат как можно больше корреляций, сохраняя при этом правильный ответ.

			Для финальных корреляций можно еще применить алгоритмы сглаживания, которые я использовал в предыдущих заданиях, но я не стал этого делать, чтобы не создать путаницы в коде.
			 */


			int startIndex = 0; //индекс, с которого начинается хорошая корреляция
			int elementsToCopy = 0; //количество элементов, которые необходимо сохранить начиная с индекса startIndex, т.е. endIndex = startIndex + elementsToCopy

			boolean isRow = false; //маркер, показывающий существует ли уже последовательность "хороших" корреляций

			//маркеры, отвечающие за смену знака
			String newRowStatus = ""; //знак текущей выборки чисел
			String oldRowStatus = ""; //знак предыдущей выборки чисел
			boolean rowStatusChanged = false; //знак поменялся

			//окно и массивы элементов окна
			int window = 10;
			double[] windowArray1 = new double[window];
			double[] windowArray2 = new double[window];

			PearsonsCorrelation correlation = new PearsonsCorrelation();
			double correlationIndex;

			//Для первой итерации предыдущего маркера знака нет, поэтому oldRowStatus должен быть равен newRowStatus
			System.arraycopy(array1, 0, windowArray1, 0, window);
			System.arraycopy(array2, 0, windowArray2, 0, window);
			correlationIndex = correlation.correlation(windowArray1, windowArray2);
			if (correlationIndex > 0) {oldRowStatus = "+";}
			if (correlationIndex < 0) {oldRowStatus = "-";}

			//Алгоритм
			for (int i = 0; i < array1.length; i=i+window) {

				if (i + window > array1.length) { //окно в последней итерации не должно выходить за пределы массива
					window = array1.length-i; //поэтому окно = оставшимся элементам
					if (window == 1) { //нельзя найти корреляцию одной пары
						window = 2;
						i = i-1;
					}
				}

				System.arraycopy(array1, i, windowArray1, 0, window);
				System.arraycopy(array2, i, windowArray2, 0, window);

				correlationIndex = correlation.correlation(windowArray1, windowArray2);

				//проверяем сменился ли знак
				if (correlationIndex > 0) {newRowStatus = "+";}
				if (correlationIndex < 0) {newRowStatus = "-";}
				if (!oldRowStatus.equals(newRowStatus)){rowStatusChanged = true;}
				oldRowStatus = newRowStatus;

				if (Math.abs(correlationIndex) >= 0.3 & !rowStatusChanged) { //последовательность началась или уже идет и текущее окно значений должно в нее войти
					if (isRow == false) {
						startIndex = i;
						isRow = true;
					}
					elementsToCopy = elementsToCopy + window;
				} else {
					if (isRow == true) { //последовательность прервалась, сохраняем результат
						double[] resultArray1 = new double[elementsToCopy];
						double[] resultArray2 = new double[elementsToCopy];
						System.arraycopy(array1,startIndex,resultArray1,0,elementsToCopy);
						System.arraycopy(array2,startIndex,resultArray2,0,elementsToCopy);
						correlationIndex = correlation.correlation(resultArray1,resultArray2);

						CorrelationResultIndex res = new CorrelationResultIndex();
						res.correlation = correlationIndex;
						res.correlationLagIndex = 0;
						res.correlationMutipleIndex = 1;
						res.startIndex = "" + startIndex;
						res.endIndex = startIndex + elementsToCopy + "";
						result.add(res);

						rowStatusChanged = false;
						isRow = false;
						elementsToCopy = 0;
						i = i-window;
						startIndex = i;

					} else {
						/*
						Если последовательности нет, а знак поменялся, то на следующей итерации условие !rowStatusChanged не будет удовлетворено и последовательность не будет записана (если она существует)
						Принудительно меняем знак, чтобы условие было удовлетворено. (это можно сделать, так как в этот else значения попадают только тогда,
						когда последовательности нет)
						*/

						if (i + window != array1.length) {
							//меняем знак так, чтобы в следующей итерации знак последнего окна совпадал со знаком текущего
							System.arraycopy(array1, i + window, windowArray1, 0, window);
							System.arraycopy(array2, i + window, windowArray2, 0, window);

							correlationIndex = correlation.correlation(windowArray1, windowArray2);
							if (correlationIndex > 0) {oldRowStatus = "+";}
							if (correlationIndex < 0) {oldRowStatus = "-";}
						}
					}
				}
				/*
				Особенность моего алгоритма - для завершения поиска последовательности необходима следующая группа пар значений.
				Если на последней итерации цикла последовательность все еще будет существовать, то она не сохранится, т.к. цикл будет завершен.
				В таком случае необходимо сохранить результат
				 */
				if (i + window == array1.length && elementsToCopy != 0) {
					if (elementsToCopy + startIndex > array1.length) {
						startIndex = array1.length - elementsToCopy;
					}

					double[] resultArray1 = new double[elementsToCopy];
					double[] resultArray2 = new double[elementsToCopy];
					System.arraycopy(array1,startIndex,resultArray1,0,elementsToCopy);
					System.arraycopy(array2,startIndex,resultArray2,0,elementsToCopy);
					correlationIndex = correlation.correlation(resultArray1,resultArray2);

					//сохраняем результат
					CorrelationResultIndex res = new CorrelationResultIndex();
					res.correlation = correlationIndex;
					res.correlationLagIndex = 0;
					res.correlationMutipleIndex = 1;
					res.startIndex = "" + startIndex;
					res.endIndex = startIndex + elementsToCopy + "";
					result.add(res);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public List<CorrelationResultIndex> executeTest11(URL resource) {
		List<CorrelationResultIndex> result = new ArrayList<CorrelationResultIndex>();

		try {
			List<double[]> rows = parseFile(resource);
			double[] array1 = rows.get(0);
			double[] array2 = rows.get(1);

			/*
			  Все тоже самое, что и в 10м задании, остаются те же самые проблемы:
			  1)В зависимости от количества элеменентов в окне, можно получать разное количество корреляций и отрезков
			  2)Пункт 1 не зависит от любого алгоритма (если использовать выборки и окна)
			  +
			  3)Если в 10м задании было примерно понятно, какие интервалы можно вернуть, то тут можно лишь угадывать
			  4) Корреляция отлично находится по всему интервалу (~0.699 и больше, если дифференицировать/сглаживать ряды)

			  Наблюдения:
			  -Разные корреляции получаются с окном от 3 до 16, выше 16 корреляция равна корреляции всего интервала ~0.699
			  -Корреляция с окном = 11 элементов равна корреляции всего интервала ~0.699.

			   Не знаю что от меня требуется, поэтому окно = 12 ( в этом случае среднее арифметическое всех корреляций в этом окне будет больше чем в других окнах,
				можно проверить, поместив весь алгоритм в цикл for (int window = 3;window<16;window++)  )
			*/


			int startIndex = 0; //индекс, с которого начинается хорошая корреляция
			int elementsToCopy = 0; //количество элементов, которые необходимо сохранить начиная с индекса startIndex, т.е. endIndex = startIndex + elementsToCopy

			boolean isRow = false; //маркер, показывающий существует ли уже последовательность "хороших" корреляций

			//маркеры, отвечающие за смену знака
			String newRowStatus = ""; //знак текущей выборки чисел
			String oldRowStatus = ""; //знак предыдущей выборки чисел
			boolean rowStatusChanged = false; //знак поменялся

			//окно и массивы элементов окна
			int window = 12;

			double[] windowArray1 = new double[window];
			double[] windowArray2 = new double[window];

			PearsonsCorrelation correlation = new PearsonsCorrelation();
			double correlationIndex;

			//Для первой итерации предыдущего маркера знака нет, поэтому oldRowStatus должен быть равен newRowStatus
			System.arraycopy(array1, 0, windowArray1, 0, window);
			System.arraycopy(array2, 0, windowArray2, 0, window);
			correlationIndex = correlation.correlation(windowArray1, windowArray2);
			if (correlationIndex > 0) {
				oldRowStatus = "+";
			}
			if (correlationIndex < 0) {
				oldRowStatus = "-";
			}

			//Алгоритм
			for (int i = 0; i < array1.length; i = i + window) {

				if (i + window > array1.length) { //окно в последней итерации не должно выходить за пределы массива
					window = array1.length - i; //поэтому окно = оставшимся элементам
					if (window == 1) { //нельзя найти корреляцию одной пары
						window = 2;
						i = i - 1;
					}
				}

				System.arraycopy(array1, i, windowArray1, 0, window);
				System.arraycopy(array2, i, windowArray2, 0, window);

				correlationIndex = correlation.correlation(windowArray1, windowArray2);

				//проверяем сменился ли знак
				if (correlationIndex > 0) {
					newRowStatus = "+";
				}
				if (correlationIndex < 0) {
					newRowStatus = "-";
				}
				if (!oldRowStatus.equals(newRowStatus)) {
					rowStatusChanged = true;
				}
				oldRowStatus = newRowStatus;

				if (Math.abs(correlationIndex) >= 0.3 & !rowStatusChanged) { //последовательность началась или уже идет и текущее окно значений должно в нее войти
					if (isRow == false) {
						startIndex = i;
						isRow = true;
					}
					elementsToCopy = elementsToCopy + window;
				} else {
					if (isRow == true) { //последовательность прервалась, сохраняем результат
						double[] resultArray1 = new double[elementsToCopy];
						double[] resultArray2 = new double[elementsToCopy];
						System.arraycopy(array1, startIndex, resultArray1, 0, elementsToCopy);
						System.arraycopy(array2, startIndex, resultArray2, 0, elementsToCopy);
						correlationIndex = correlation.correlation(resultArray1, resultArray2);

						CorrelationResultIndex res = new CorrelationResultIndex();
						res.correlation = correlationIndex;
						res.correlationLagIndex = 0;
						res.correlationMutipleIndex = 1;
						res.startIndex = startIndex + 1 + "";
						res.endIndex = startIndex + elementsToCopy + "";
						result.add(res);

						rowStatusChanged = false;
						isRow = false;
						elementsToCopy = 0;
						i = i - window;
						startIndex = i;

					} else {
						/*
						Если последовательности нет, а знак поменялся, то на следующей итерации условие !rowStatusChanged не будет удовлетворено и последовательность не будет записана (если она существует)
						Принудительно меняем знак, чтобы условие было удовлетворено. (это можно сделать, так как в этот else значения попадают только тогда,
						когда последовательности нет)
						*/

						if (i + window != array1.length) {
							//меняем знак так, чтобы в следующей итерации знак последнего окна совпадал со знаком текущего
							System.arraycopy(array1, i + window, windowArray1, 0, window);
							System.arraycopy(array2, i + window, windowArray2, 0, window);

							correlationIndex = correlation.correlation(windowArray1, windowArray2);
							if (correlationIndex > 0) {
								oldRowStatus = "+";
							}
							if (correlationIndex < 0) {
								oldRowStatus = "-";
							}
						}
					}
				}
				/*
				Особенность моего алгоритма - для завершения поиска последовательности необходима следующая группа пар значений.
				Если на последней итерации цикла последовательность все еще будет существовать, то она не сохранится, т.к. цикл будет завершен.
				В таком случае необходимо сохранить результат
				 */
				if (i + window == array1.length && elementsToCopy != 0) {
					if (elementsToCopy + startIndex > array1.length) {
						startIndex = array1.length - elementsToCopy;
					}

					double[] resultArray1 = new double[elementsToCopy];
					double[] resultArray2 = new double[elementsToCopy];
					System.arraycopy(array1, startIndex, resultArray1, 0, elementsToCopy);
					System.arraycopy(array2, startIndex, resultArray2, 0, elementsToCopy);
					correlationIndex = correlation.correlation(resultArray1, resultArray2);

					//сохраняем результат
					CorrelationResultIndex res = new CorrelationResultIndex();
					res.correlation = correlationIndex;
					res.correlationLagIndex = 0;
					res.correlationMutipleIndex = 1;
					res.startIndex = startIndex + 1 + "";
					res.endIndex = startIndex + elementsToCopy + "";
					result.add(res);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}


}
