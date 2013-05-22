﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace amp.rabbit.topology
{
    public class RouteInfo
    {
        public Exchange ProducerExchange { get; set; }
        public Exchange ConsumerExchange { get; set; }


        public RouteInfo()
        {
        }

        public RouteInfo(Exchange producerExchange, Exchange consumerExchange)
        {
            this.ProducerExchange = producerExchange;
            this.ConsumerExchange = consumerExchange;
        }
    }
}
